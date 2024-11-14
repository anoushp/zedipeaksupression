package zedipeaksupression.agents;

import java.util.ArrayList;

import zedipeaksupression.Topology;

public class BuildingCentralController {
		private double[] dryBulbAirTemp;
		private double[] infraredRadiation;
		private double[] diffuseSolarRadiation;
		private double[] directSolarRadiation;
		
		private ArrayList<Flat> flats;
		public static String network_name;
		private Topology topology;
		public double[] getDryBulbAirTemp() {
			return dryBulbAirTemp;
		}
		public void setDryBulbAirTemp(double[] dryBulbAirTemp) {
			this.dryBulbAirTemp = dryBulbAirTemp;
		}
		public double[] getInfraredRadiation() {
			return infraredRadiation;
		}
		public void setInfraredRadiation(double[] infraredRadiation) {
			this.infraredRadiation = infraredRadiation;
		}
		public double[] getDiffuseSolarRadiation() {
			return diffuseSolarRadiation;
		}
		public void setDiffuseSolarRadiation(double[] diffuseSolarRadiation) {
			this.diffuseSolarRadiation = diffuseSolarRadiation;
		}
		public double[] getDirectSolarRadiation() {
			return directSolarRadiation;
		}
		public void setDirectSolarRadiation(double[] directSolarRadiation) {
			this.directSolarRadiation = directSolarRadiation;
		}
		public ArrayList<Flat> getFlats() {
			return flats;
		}
		public void setFlats(ArrayList<Flat> flats) {
			this.flats = flats;
		}
		public Topology getTopology() {
			return topology;
		}
		public void setTopology(Topology topology) {
			this.topology = topology;
		}
		
		
		/**
		 * Implementation of the agent activity in each turn.
		 * 
		 */
		public void step(int step) {
			
		}
		
		

}
