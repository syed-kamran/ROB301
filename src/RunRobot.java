import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;


public class RunRobot {
	
	private enum PizzaLocation {
        left, right;
    } 
	
	private enum RoadSide {
		left, right; 
	}
	
	private enum RoadColor {
		red, green, blue;
	}
	
	private enum HouseNumber {
		first, second, third;
	}
	
	private static PizzaLocation Pizzaria;
	private static RoadColor Road;
	private static HouseNumber House;
	private static RoadSide Side;
	

	public static void main(String[] args){
		
		DifferentialPilot pilot = new DifferentialPilot(2.16535f, 6.2f, Motor.C, Motor.B);
		EV3UltrasonicSensor sonic = new EV3UltrasonicSensor(SensorPort.S1);
		EV3ColorSensor color = new EV3ColorSensor(SensorPort.S4);
		
		obtainPizzariaInformation();
		obtainRoadColor();
		obtainRoadSideInformaiton();
		obtainHouseNumber();
				
		LCD.drawString(Pizzaria.toString() + " " + Road.toString() + " " + House.toString(), 0, 0);
		LCD.drawString("Road Side: " + Side.toString(), 0, 1);
		
		Button.waitForAnyPress();
//		
		
		
		Motor.A.setSpeed(70);
		
		pilot.setTravelSpeed(5);
		pilot.setRotateSpeed(50);
		

		travelToPizza(pilot);
		returnToMain(pilot);
		
//		Button.ENTER.waitForPress();
		
		
		gotoLanes(pilot, sonic, color);
		
		
		//we're at the blue
		//goto to right color
		
		gotoRoad(pilot);
		
		gotoHouse(pilot);
		
		dropPizzaAtHouse(pilot);
		
	}
	
	private static void dropPizzaAtHouse(DifferentialPilot pilot){
		
		switch (Side){
			case left:
				pilot.rotate(-90);
				dropPizza();
			case right:
				pilot.rotate(90);
				dropPizza();
			default:
				break;
		}
		
	}
	
	private static void gotoHouse(DifferentialPilot pilot){
		
		//rotate ultrasonic
		if(Side.equals(RoadSide.left)){
			rotateSonicLeft();
		}
		else{
			rotateSonicRight();
		}
		
		switch(House){
			case first:
				pilot.travel(10);
				break;
			case second:
				pilot.travel(15);
				break;
			case third:
				pilot.travel(20);
				break;
			default:
				break;
			
		}
	}
	
	private static void gotoRoad(DifferentialPilot pilot){
		
		if(Road.equals(RoadColor.blue)){
			Sound.beep();
		}
		else if(Road.equals(RoadColor.green)){
			pilot.rotate(90);
			pilot.travel(12);
			pilot.rotate(-60);
		}
		else{
			pilot.rotate(-90);
			pilot.travel(12);
			pilot.rotate(60);
		}
		
	}
	
	private static float rotateSonicPartialLeft(EV3UltrasonicSensor sonic){
		Motor.A.rotate(43);
		float distance =  ultrasonic(sonic);
		Motor.A.rotate(-50);
		return distance;
		
	}
	
	private static float rotateSonicPartialRight(EV3UltrasonicSensor sonic){
		Motor.A.rotate(-50);
		float distance =  ultrasonic(sonic);
		Motor.A.rotate(50);
		return distance;	
	}
	
	private static void rotateSonicRight(){
		Motor.A.rotate(-97);
		
	}
	private static void rotateSonicFromRight(){
		Motor.A.rotate(100);
	}
	
	private static void rotateSonicLeft(){
		Motor.A.rotate(95);
	}
	private static void rotateSonicFromLeft(){
		Motor.A.rotate(-100);
	}

	
	
	private static void rotateSonic(){
		Motor.A.setSpeed(50);
		Motor.A.rotate(115);
		Motor.A.rotate(-115);
		Motor.A.rotate(-105);
	}
	
	private static void avoidObstacle(DifferentialPilot pilot,  EV3UltrasonicSensor sonic){
		float distance_left = rotateSonicPartialLeft(sonic);
		float distance_right = rotateSonicPartialRight(sonic);
		
		if(distance_left > distance_right){
			//turn left
			pilot.rotate(90);
			
			//rotate sonic right
			rotateSonicRight();
			
			//follow this path till end of obstacle
			int go_back = follow_obstacle(pilot, sonic);
			
			pilot.rotate(-90);
			rotateSonicFromRight();
			pilot.travel(20);
			pilot.rotate(-90);
			pilot.travel(go_back+9.25);
			pilot.rotate(90);
		}
		else{
			//turn right
			pilot.rotate(-90);
			
			//rotate sonic left 
			rotateSonicLeft();
			
			//follow this path till end of obstacle
			int go_back = follow_obstacle(pilot, sonic);
			
			pilot.rotate(90);
			rotateSonicFromLeft();
			pilot.travel(20);
			pilot.rotate(90);
			pilot.travel(go_back+9.25);
			pilot.rotate(-90);
			
		}
		
	}
	
	private static void gotoLanes(DifferentialPilot pilot,  EV3UltrasonicSensor sonic, EV3ColorSensor color) {
		
		while (ultrasonic(sonic) > 10.0f && colorInfo(color) > 0.05) {
			float distance = ultrasonic(sonic);
			System.out.println(distance);
			pilot.travel(2);
		}		
		pilot.stop();
		
		//obstacle was detected
		if(ultrasonic(sonic) < 10.0f){
			avoidObstacle(pilot, sonic);
			gotoLanes(pilot, sonic, color);
		}
		
		
	}
	
	private static int follow_obstacle(DifferentialPilot pilot, EV3UltrasonicSensor sonic){
		int count = 0;
		while (ultrasonic(sonic) < 65.0f) {
			pilot.travel(1);
			count++;
		}
		pilot.travel(9);
		pilot.stop();
		
		return count;
	}
	
	private static float ultrasonic(EV3UltrasonicSensor sonic){
		
		int sampleSize = sonic.sampleSize();
		float[] sonicsample = new float[sampleSize]; 
		sonic.fetchSample(sonicsample, 0); 
		LCD.clear(); 
		return sonicsample[0]*100;
		
	}
	
	private static void travelToPizza(DifferentialPilot pilot){
		
		if(Pizzaria.equals(PizzaLocation.right)){
			pilot.travel(12);
			pilot.rotate(90);
		}
		else{
			pilot.travel(11);
			pilot.rotate(-90);
		}
		pilot.travel(-18);
		liftPizza();
		
	}
	
	
	private static void liftPizza(){
		Motor.D.setSpeed(50);
		Motor.D.rotate(-50);
	}
	
	private static void dropPizza(){
		Motor.D.setSpeed(50);
		Motor.D.rotate(50);
	}
	
	private static void returnToMain(DifferentialPilot pilot){
		pilot.travel(18);
		if(Pizzaria.equals(PizzaLocation.right)){
			pilot.rotate(-90);
		}
		else{
			pilot.rotate(90);
		}
	}
	
	private static void obtainPizzariaInformation(){
		
		
		LCD.drawString("Pizza Direction?", 0, 0);
		int pizza_choice = Button.waitForAnyPress();
		
		switch(pizza_choice) {
			case Button.ID_LEFT : 
				Pizzaria = PizzaLocation.left;
				break;
			case Button.ID_RIGHT :
				Pizzaria = PizzaLocation.right;
				break;	
			default:
				break;		
		}
		LCD.clear();
		
	}
	
	private static void obtainRoadSideInformaiton(){
		
		
		LCD.drawString("Road Side?", 0, 0);
		int side_choice = Button.waitForAnyPress();
		
		switch(side_choice) {
			case Button.ID_LEFT : 
				Side = RoadSide.left;
				break;
			case Button.ID_RIGHT :
				Side = RoadSide.right;
				break;	
			default:
				break;		
		}
		LCD.clear();
		
	}
	
	private static void obtainRoadColor(){
		
		LCD.drawString("Road colour?", 0, 0);
		int road_choice = Button.waitForAnyPress();
		
		switch(road_choice) {
			case Button.ID_LEFT : 
				Road = RoadColor.green;
				break;
			case Button.ID_RIGHT :
				Road = RoadColor.red;
				break;
			case Button.ID_UP :
				Road = RoadColor.blue;
				break;	
			default:
				break;		
		}
		LCD.clear();
		
	}
	
	private static void obtainHouseNumber(){
		
		LCD.drawString("House number?", 0, 0);
		int house_choice = Button.waitForAnyPress();
		
		switch(house_choice) {
			case Button.ID_LEFT : 
				House = HouseNumber.first;
				break;
			case Button.ID_RIGHT :
				House = HouseNumber.third;
				break;
			case Button.ID_UP :
				House = HouseNumber.second;
				break;	
			default:
				break;		
		}
		LCD.clear();
		
	}
	
	private static float colorInfo(EV3ColorSensor color){
		
		int sampleSize = color.sampleSize();
		float[] idsample = new float[sampleSize]; 
		color.getRedMode().fetchSample(idsample, 0); 
		return idsample[0];
	}
	
}
