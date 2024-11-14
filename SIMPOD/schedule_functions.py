#!/usr/bin/python
from random import randint
import sys
import pandas as pd, numpy as np
import datetime, time
import gspread
from oauth2client.service_account import ServiceAccountCredentials


def login_open_sheet(oauth_key_file, spreadsheet):
    """Connect to Google Docs spreadsheet and return the first worksheet."""
    try:
        scope =  ['https://spreadsheets.google.com/feeds', 'https://www.googleapis.com/auth/drive']
        credentials = ServiceAccountCredentials.from_json_keyfile_name(oauth_key_file, scope)
        gc = gspread.authorize(credentials)
        book = gc.open(spreadsheet) # pylint: disable=redefined-outer-name
        return book
    except Exception as ex: # pylint: disable=bare-except, broad-except
        print('Unable to login and get spreadsheet.  Check OAuth credentials, spreadsheet name, \
        and make sure spreadsheet is shared to the client_email address in the OAuth .json file!')
        print('Google sheet login failed with error:', ex)
        sys.exit(1)
        
 
def makesched(sheetvals, heatshift=2):
    '''make short version of schedule for rescheduling purposes'''
    #heatshift=2 # plus or minus heating hours
    cols = sheetvals[1] # Names of demands
    cols[0]="Event" # rename empty first column
    df = pd.DataFrame(sheetvals[2:], columns=cols)
    df.set_index("Event", inplace=True) 
    
    apps = list(df.columns)
    power=dict(df[0:1]) #extract the power values
    df = df[1:] #remove the power row

    tabon = df[df.index=='Time On'] # separate on events
    taboff = df[df.index=='Time Off'] # separate off events

    sched = tabon.melt(var_name='Demand', value_name='Time On') # un-pivot
    s2 = taboff.melt(var_name='Demand', value_name='Time Off') # un-pivot
    sched['Time On'] = pd.to_datetime(sched['Time On'])
    sched["Time Off"]=pd.to_datetime(s2["Time Off"].values) # splice in off events to main schedule

    sched.dropna(inplace=True) # drop empty entries
    #sched['Duration']=sched['Time Off']-sched['Time On'] # use to check it isn't only 15 min

    sched["Load"]=[float(power[ap]) for ap in sched.Demand] # set the loads column

    # reset heating delays so they appear offset and can be advanced by an hour if needed
    sched["Delay"]=[datetime.timedelta(hours=heatshift) if isheating 
                    else datetime.timedelta() for isheating in sched.Demand=='Heating']
    return sched

        
        
def get_worksheet(book, ID, rtime, start, interval=15):
    pars = book.worksheet("Params").get_all_records()[0]
    heatsh = pars['heatLimit']

    while rtime>start: #try until a return, break or error raised    
        # try to use last working copy
        prevtime=rtime-datetime.timedelta(hours=interval/60)
        try: 
            sched=pd.read_csv(f"Schedules/sched_{ID}_{prevtime}.csv")
            sched.loc[:, 'Time On' ] = pd.to_datetime(sched['Time On'])
            sched.loc[:, 'Time Off'] = pd.to_datetime(sched['Time Off'])
            sched.loc[:, 'Delay' ]   = pd.to_timedelta(sched['Delay'])
            '''
            # reformat the sheduling data for updating the long schedule
            schedule = reformat_sched(sched, interval) # returns new long scheduler
            # send long schedule back to google
            push_long_schedule(schedule, book, ID)
            '''
            return sched # exits on success
        except FileNotFoundError:
            # try previous file 
            rtime=rtime-datetime.timedelta(hours=interval/60)
            print(f'reverting to {rtime}')
        if rtime.strftime(format='%Y-%m-%d %H:%M:00')==rtime.strftime(format='%Y-%m-%d 00:00:00'):
            break
            # continue
    #if rtime==start: 
    '''pulls original sheet if at start time'''
    sheet = book.worksheet(ID)
    sheetvals = sheet.get_all_values()
    
    # make compact schedule for shifting alg to work on
    sched = makesched(sheetvals,heatshift=heatsh)
    # save the original schedule to disk
    sched.to_csv(f"Schedules/sched_{ID}.csv", index=False) 
    
    # process original schedules to long format
    longsched = process_schedule(sheetvals, interval=interval)
    # write long schedule back to google
    push_long_schedule(longsched, book, ID) 
    
    time.sleep(1)
    
    # pull the neighbourhood totals sheet
    neisheet = book.worksheet('NEIGH').get_all_records()
    tots = pd.DataFrame(neisheet).set_index("DateTime") # pull totals
    # calculate first time but just take same value from then on 
    maxval = tots[f'TOTAL_{ID}'].max()
    with open('maxval.txt', 'w') as maxf:
        maxf.write(str(maxval))

    return sched         
         
        


def process_schedule(sheetvals, interval=15):
    """
    Function to read in a google sheet in format below and convert to a schedule table.
    ---------------------------------------------------------------------------------
    ignored row: Relay 1    Relay 2            Relay 3        Relay 4    
                 Heating    Washing Machine    Dish Washer    Dryer      baseload
    wattage      1000       800                400            500        100
    Time On      09:00:30
    Time Off     11:00:00
    ---------------------------------------------------------------------------------
    returns a minute-by-minute schedule table with column values 1 for on 0 for off
    """
    cols = sheetvals[1]
    #print(cols)
    cols[0]="Event"
    df = pd.DataFrame(sheetvals[2:], columns=cols)
    df.set_index("Event", inplace=True) 
    
    apps = list(df.columns)
    power=dict(df[0:1]) #extract the power values
    #print(power)
    df = df[1:] #remove the power row
    
    # set up start and end times for the schedule table
    now=datetime.datetime.now() # get the current datetime
    start=now.strftime(format='%Y-%m-%d 00:00:00') # set the start of the day
    then=now+datetime.timedelta(days=1) # add a day for the end time
    end = then.strftime(format='%Y-%m-%d 00:00:00') # string for the end of the day
    
    relays=[] # new list for each of the relays
    for ap in apps: # for each of the appliance columns in the spreadsheet
        r = df[ap].to_frame() # make a new dataframe
        r.columns = "Times", # rename the column to "Times"
        #r["Event"] = df["Event"] # set the "Time On"/"Time Off" Events column
        # make sure all entries are either a timestamp or an integer (hour)
        r["Times"] = [pd.to_datetime(t) if type(t)==str #datetime.time 
                        else f'{t}:00:00' if (type(t)==int and 0<=t<24) 
                        else None 
                        for t in r["Times"]]
        r = r[r.Times.isnull()==False]  # get rid of entries that are not times
        # make a new column with the name of the appliance from the spreadsheet
        # with values of 1 or 0 if the device is being switched on or off
        p = float(power[ap])
        r[ap] = [p if e=="Time On" else 0 if e=="Time Off" else None for e in r.index]
        #r.drop("Event", axis=1, inplace=True) # remove the Event column
        # convert to a string of timestamps (uses the current date)
        r["Times"] = pd.to_datetime(r["Times"].astype(str)) 
        r.set_index("Times", inplace=True) # set this as the index
        duplicates = r.index.duplicated(keep='last') # flag the duplicates
        r=r[duplicates==False] # remove all but the last time duplicate for each device
        # resample to one entry per minute
        r = r.resample(f'{interval}min').fillna("pad") # fill in values following previous 1/0
        # reindex so that all minutes in the day have an entry in the dataframe
        r = r.reindex(pd.date_range(start=start, end=end, freq=f"{interval}min"), method="pad")
        relays.append(r) # put this applicance dataframe in the relays list

    schedule = pd.concat(relays, axis=1) # join into a single dataframe
    schedule["Total"]=schedule.sum(axis=1)
    schedule.index.name = 'DateTime'
    #print(max(schedule.Total))
    #return schedule.fillna(0) # return the schedule with non-entries filled as 0
    sched = schedule.fillna(0)
    
    return sched


def reformat_sched(sched, interval=15):
    '''Create original schedule format with new version'''
    #sheet = book.worksheet(ID)
    cols = ['Event', 'Heating', 'Hot Water', 'Washing Machine', 'Dryer', 'baseload']
    toprow = [['watts', '1000', '480', '250', '360', '100']]
    newdf = pd.DataFrame(toprow, columns=cols)
    for idx in sched.index:
        line = sched[sched.index==idx]
        demand = line['Demand'][idx]
        timeon = pd.to_datetime(line['Time On'][idx])
        timeoff = pd.to_datetime(line['Time Off'][idx])
        newcol = {'Event':'Time On', demand:timeon.strftime('%H:%M:%S')}
        newdf = newdf.append(newcol, ignore_index=True)
        newcol = {'Event':'Time Off', demand:timeoff.strftime('%H:%M:%S')}
        newdf = newdf.append(newcol, ignore_index=True)
    for col in newdf: # shift everything up
        nonnan = list(newdf[col].dropna().values)
        newdf[col]=nonnan+[None]*(len(newdf)-len(nonnan))
    newdf = newdf.set_index("Event").dropna(how='all').reset_index()
    cols = ['', 'R1', 'R2', 'R3', 'R4', '']
    newvals = [cols] + [[''] + newdf.columns.values.tolist()[1:]] + newdf.values.tolist()
    
    schedule = process_schedule(newvals, interval=interval)
    
    return schedule



def push_long_schedule(s, book, ID):
    ''' write the switching schedule back to google sheets '''
    s = s.fillna(0)
    s.insert(0, "DateTime", s.index.strftime("%Y-%m-%d %H:%M:%S"))
    try: newsheet = book.add_worksheet(title=f"Schedule-{ID}", rows="1", cols="1")
    except: pass # if it already exists
    newsheet = book.worksheet(f"Schedule-{ID}")
    newsheet.update([s.columns.values.tolist()] + s.values.tolist()) # push schedule sheet to google sheets



####################################
#                                  #
# Main algorithm for load shifting #
#                                  #
####################################

def resched(sched, limit, prevval, rtime, maxhours = 6, heatshift=2, interval=15):
    '''
    Main algorithm for load shifting
    '''     
    maxdelay = datetime.timedelta(hours=maxhours) # hours
    heatdelay=datetime.timedelta(hours=heatshift) # hours
    # ensure everything is in datetime format
    sched['Time On']=pd.to_datetime(sched['Time On'])
    sched['Time Off']=pd.to_datetime(sched['Time Off'])
    gap = prevval-limit
    newgap=0
    while abs(newgap)<abs(gap):
        gap=prevval-limit
        print(gap, end=' => ')
        if gap > 0:
            print('Above limit')
            diffgap = (gap-sched['Load']).abs()
            suitable =  diffgap < abs(gap) # shifting will get closer to limit
            
            mind=interval/60 # 0.25 # 15 minutes is smallest shift
            mindelay = datetime.timedelta(hours=mind) # set minimum delay
            # shifting will not exceed max delay & is not a baseload
            shifteddelay = sched['Delay']+mindelay
            shiftable = (shifteddelay<=maxdelay) & (sched['Demand']!='baseload') & (sched['Demand']!='Heating')
            shiftedheat = sched['Delay']+mindelay
            shiftableheat = (shiftedheat<=heatdelay) & (sched['Demand']=='Heating')
            
            # look what is currently switched on or about to be:
            # note sched['Time On']<=rtime would allow pre-emptive interruption
            dtimeon = pd.to_datetime(sched['Time On'])
            dtimeoff = pd.to_datetime(sched['Time Off'])
            current = (dtimeon<rtime) & (rtime<dtimeoff) 
            
            interuptable = sched[shiftable & current & suitable] 
            interuptableheat = sched[shiftableheat & current & suitable] 
            if len(interuptableheat)>0:
                interuptable.append(interuptableheat)
            
            interuptable.loc[:, 'Diff'] = gap - interuptable['Load']
            
            ###select load likely to bring us closest to the threshold...
            interuptable = interuptable.sort_values(by=['Diff'], key=abs)

            if len(interuptable)>0: # something can be shifted
                iload = interuptable.index[0] # get top of all interuptable loads
                shifted = sched[sched.index==iload] # copy current load to shifted
                
                #determine what the max allowed shift is: h=maxdelay-currentdelay
                delayval = shifted['Delay'].values[0]/np.timedelta64(1, 'h')
                theshifteddemand = shifted['Demand'].values[0]
                if theshifteddemand =='Heating': 
                    maxshift = heatshift - delayval
                else: maxshift = maxhours - delayval
                
                # turn off the current load now in schedule
                sched.loc[iload, 'Time Off']=rtime 

                # determine new shift amount randomly
                shift = datetime.timedelta(hours=mind*randint(1,round(maxshift/mind))) 
                # shift selected load to start at now + shift
                shifted.loc[:, 'Time On'] = rtime+shift 
                # offset stop time by shift
                shifted.loc[:, 'Time Off'] = shifted['Time Off']+shift 
                # increment the recorded delay by shift amount
                shifted.loc[:, 'Delay'] = shifted['Delay']+shift 
                
                # put at end of the scheduling table
                sched = sched.append(shifted, ignore_index=True) 
                
                shiftedload = shifted['Load'].values[0]
                prevval = prevval-shiftedload # recalculate the current load

        elif gap < 0:
            print('Below limit')
            diffgap = (gap+sched['Load']).abs()
            suitable =  diffgap < abs(gap) # shifting will get closer to limit
            ### allow to get as close to limit (above or below) as possible.
            
            delaytimes = sched['Delay']
            dtimeon = pd.to_datetime(sched['Time On'])
            in_pool = (delaytimes>datetime.timedelta(0)) & (dtimeon>=rtime) # excludes baseload
            # using >=rtime in last part includes about to come on loads...
            # delay>0 means offset or can be advanced (heating), & check on in future
            shiftedtimeon = sched['Time On']-sched['Delay']
            shiftable = shiftedtimeon<=rtime  # Heating cannot be brought forward more than current delay value
            
            #available = sched[suitable & in_pool & shiftable]
            available = sched[in_pool & shiftable & suitable]
            
            # what the gap will be reduced to by shifting this load
            available.loc[:, 'Diff'] = available['Load'] - gap
            
            ###select load likely to bring us closest to the threshold...
            available = available.sort_values(by=['Diff'], key=abs)
            if len(available)>0: #something can be shifted
                iload = available.index[0] # get top of all available loads
                shifted = sched[sched.index==iload] # copy current load to shifted
                
                # shift the load in the schedule
                shift = available['Time On'] - rtime
                sched.loc[iload, 'Time On']=rtime
                sched.loc[iload, 'Time Off']=(shifted['Time Off']-shift).values[0]
                sched.loc[iload, 'Delay']=(shifted['Delay']-shift).values[0]
                
                shiftedload = shifted['Load'].values[0]
                prevval = prevval+shiftedload # recalculate the current load

        newgap = prevval-limit

    return sched
    