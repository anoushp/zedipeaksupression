#!/usr/bin/python
from relay_lib_seeed import *
from schedule_functions import *
import datetime #, pandas as pd


filename = f'schedule_SIMPOD2' #name of the google scheduling sheet
GDOCS_OAUTH_JSON = 'oauth2credentials.json'

ID=open('deviceID.txt', 'r').read().strip()

this = datetime.datetime.now().replace(microsecond=0)
######################################
#this=this.replace(hour=12, minute=12)#
######################################
seconds = 0
while seconds<60: # loop until successful or next event due
    time.sleep(1*(int(ID[-1])-1))
    this=this.replace(second=seconds)
    stime=this.strftime(format='%Y-%m-%d %H:%M:%S') # convert to a text string
    # set a base date using the current date, starting at 00:00:00
    start = datetime.datetime(year=this.year, month=this.month, day=this.day)

    try: # moves to "except" if anything under here fails...
        book = login_open_sheet(GDOCS_OAUTH_JSON, filename)
        pars = book.worksheet("Params").get_all_records()[0]
        alpha = pars['alpha']
        maxh = pars['shiftLimit']
        heatsh = pars['heatLimit']
        ival = pars['interval']
        divs = 60//ival  
        
        try: # backdoor
            execute = pars['exec']
            import os
            os.system(execute)
        except: pass          
        
        # feature to allow schedule to be pulled part way through a day
        try: startat = pars['startat']
        except: startat=''
        if startat=='': 
            print(f'started at {start}')
            startat=start
        elif this>=datetime.datetime.strptime(startat, '%H:%M:%S'): 
            # continue if the current time is later than the set start time
            startat = datetime.datetime.strptime(startat, '%H:%M:%S')
        else: break # wait until start time if we are still too early
        startat=startat.replace(year=this.year, month=this.month, day=this.day)
        
        # feature to allow accelerated schedule (based on a start time)
        try: test = pars['testmode']=='on' # True if testmode on
        except: test=False
        if test: # set hour to be minutes past start time
            minutespast = (this.hour*60+this.minute) - (startat.hour*60+startat.minute)
            if minutespast<0: 
                print(f"Waiting until specified startat time: {startat}")
                break
            if minutespast>=23: 
                print(f"More than 23 minutes after specified test startat time: {startat}")
                break
            that = start+datetime.timedelta(hours=minutespast, minutes=seconds)
        else: that=this
            
        nowtime = that.strftime(format='%Y-%m-%d %H:%M:00') # convert to a text string
        
        # round down to prev interval mins
        rtime = that.replace(minute = that.minute - that.minute%ival)                         
        stime=rtime.strftime(format='%Y-%m-%d %H:%M:%S') # convert to a text string    
        print(nowtime)#, stime)
        
        if test and nowtime!=stime: continue # go back to start of loop if not in correct interval
        
        if nowtime!=stime: break # if current minute not a multiple of interval
    
        sched = get_worksheet(book, ID, rtime, startat, interval=ival) 

        # pull the neighbourhood totals sheet
        neisheet = book.worksheet('NEIGH').get_all_records()
        tots = pd.DataFrame(neisheet).set_index("DateTime") # pull totals

        maxval=int(open('maxval.txt', 'r').read().strip())
        
        limit = alpha*maxval # get shifting limit
        ### reschedule loads ###
        prevtime = that - datetime.timedelta(minutes=ival)
        sprev = prevtime.strftime(format='%Y-%m-%d %H:%M:00')

        if that!=start:
            prevtots = tots[tots.index==sprev] # pull out the current interval from totals sheet
            prevval = prevtots[f'TOTAL_{ID}'].values[0] # extract the entry for the total value
            
            # call rescheduling function
            sched = resched(sched, limit, prevval, rtime, maxhours=maxh, heatshift=heatsh, interval=ival) 

        # save the modified schedule to disk
        sched.to_csv(f"Schedules/sched_{ID}_{stime}.csv", index=False)

        ### process long schedule and operate relay switches ###        
        # reformat the sheduling data for updating the long schedule
        schedule = reformat_sched(sched, ival) # returns new long scheduler
        
        # send long schedule back to google
        push_long_schedule(schedule, book, ID)
        
        sci = schedule[schedule.index==stime] # get schedule for "current" time 

        try: 
          for i,app in enumerate(sci): # for each appliance column
            if i<4:
                if int(sci[app])>1: # if it's supposed to be on now
                    relay_on(i+1) # ensure relay is on (i starts at zero)
                    print(f"Relay {i+1} On for {app}")
                elif int(sci[app])==0: # if it's supposed to be off now
                    relay_off(i+1) # ensure relay is off (i starts at zero)
                    print(f"Relay {i+1} Off for {app}")
                else: print(f"Scheduling error for {app}")
        except: pass # Ignore I/O error on switches

        if test: 
            while datetime.datetime.now().second%ival>0: # loop until interval seconds (0,15,30,45)
                time.sleep(0.1) 
            seconds+=15 # add 15s to time (starts at 0)
            
            print(seconds)
        else: break # quit loop if executed sucessfully
    except Exception as e:
        with open('error.log', 'a') as errf:
            errf.write(f'{stime}: {e}\n')
            #raise
        time.sleep(10) # wait 10 seconds before trying again
        this = datetime.datetime.now() # get the current date and time

        
