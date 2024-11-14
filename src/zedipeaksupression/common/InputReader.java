package zedipeaksupression.common;

import zedipeaksupression.framework.InputDataReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.csvreader.CsvReader;
public class InputReader implements InputDataReader{
	
	
	public void readDatafromFile(String filename) {

	}

	// reads temperature for given date and time (date variable)
	public double[][] readRoomTemperatureData(int noOfFlats, int noOfRooms, String date, String path)
			throws IOException {
		double[][] temperatureFlatsRoom = new double[24][noOfFlats * noOfRooms];
		try {
			CsvReader fileInfo = new CsvReader(path);
			int num = 0;
			fileInfo.readHeaders();
			while (fileInfo.readRecord()) {

				if (fileInfo.get("DateTime").startsWith(date)) {
					int ind = 0;
					for (int i = 0; i < noOfFlats; i++) {
						String colName = "F" + (i + 1) + "R";
						for (int j = 0; j < noOfRooms; j++) {
							int room = j + 1;
							String finColName = colName + room;
							// System.out.println(finColName);
							// System.out.println(Double.parseDouble(fileInfo.get(finColName)));
							temperatureFlatsRoom[num][ind] = Double.parseDouble(fileInfo.get(finColName));
							ind++;
						}
					}

				}
				num++;
			}
			fileInfo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return temperatureFlatsRoom;
	}

	
	// reads average electricity consumptions for each flat
	public double[][] readElectricityConsumptionAverages(int noOfFlats, String path) throws IOException {
		double[][] electricityAvgLoads = new double[24][noOfFlats];
		try {

			CsvReader fileInfo = new CsvReader(path);
			int num = 0;
			fileInfo.readHeaders();
			while (fileInfo.readRecord()) {
				
				for (int i = 0; i < noOfFlats; i++) {
					String colName = "F" + (i + 1);
					electricityAvgLoads[num][i] = Double.parseDouble(fileInfo.get(colName));

				}
				num++;
				// System.out.println("NUM " + num);

			}

			// System.out.println(electricityLoadsFlat.length);
			fileInfo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return electricityAvgLoads;
	}
		
	
	// reads base loads for given date and time (date variable)
	public double[][] readBaseLoadData(int noOfFlats, Date start, Date end, int numOfdays, String path)
			throws IOException {
		double[][] electricityLoadsFlat = new double[24 * Constants.OBS_PER_HOUR * numOfdays][noOfFlats];
		try {

			CsvReader fileInfo = new CsvReader(path);
			int num = 0;
			fileInfo.readHeaders();
			while (fileInfo.readRecord()) {
				String s = fileInfo.get("DateTime");
				// System.out.println(s);

				Date date = NetworkUtils.parseStringtoDate(s);
				// String string=sdf1.format(date);
				// System.out.println("Current date in Date Format: " + string);

				if (!start.after(date) && date.before(end)) {
					System.out.println(fileInfo.get("DateTime"));
					// String string=sdf1.format(date);
					// System.out.println("Current date in Date Format: " + string);
					for (int i = 0; i < noOfFlats; i++) {
						String colName = "F" + (i + 1);
						electricityLoadsFlat[num][i] = Double.parseDouble(fileInfo.get(colName));

					}
					num++;
					System.out.println("NUM " + num);

				}

			}
			// System.out.println(electricityLoadsFlat.length);
			fileInfo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return electricityLoadsFlat;
	}

	// reads outdoor parameters for building controller
	public double[][] readOutdoorsData(Date start, Date end, int numOfdays, String path) throws IOException {
		double[][] outdoorsconditions = new double[Constants.TIME_INTERVAL * numOfdays][4];

		String[] colnames = { "DryBulbTemp", "InfraredRadiation", "DiffuseSolarRadiation", "DirectSolarRadiation" };
		SimpleDateFormat sdf1 = new SimpleDateFormat();
		sdf1.applyPattern("MM/dd/yyyy HH:mm");

		try {

			CsvReader fileInfo = new CsvReader(path);
			int num = 0;
			fileInfo.readHeaders();
			while (fileInfo.readRecord()) {
				String s = fileInfo.get("DateTime");
				String[] dates = s.split("\\s+");
				dates[0] += "/2018";
				s = dates[0] + " " + dates[1];

				Date date = sdf1.parse(s);
				// System.out.println(s);
				if (!start.after(date) && date.before(end)) {
					// System.out.println(fileInfo.get("DateTime"));
					for (int i = 0; i < colnames.length; i++) {
						String colName = colnames[i];
						outdoorsconditions[num][i] = Double.parseDouble(fileInfo.get(colName));

					}

				}
				num++;
			}
			fileInfo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return outdoorsconditions;
	}

}
