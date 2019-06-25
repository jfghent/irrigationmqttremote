/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sstest;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
//import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
//import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import static java.time.temporal.TemporalAdjusters.previous;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;



/**
 *
 * @author Jon
 */
public class SSTest {
    
    private static MqttClient mqttClient;
    private final static String MQTT_BROKER       = "tcp://192.168.1.46:1885";
    private final static String MQTT_CLIENTID     = "mqtt-ss-ctrl";
    private static GpioController gpio;
    private static ArrayList<GpioPinDigitalOutput> zonePins;
    //public String previous;

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        
        System.out.println("Hello World");
        
        try
        {
                mqttCallback mqttRec = new mqttCallback();

                mqttClient = new MqttClient(MQTT_BROKER, MQTT_CLIENTID); //, persistence);
                mqttClient.setCallback(mqttRec);
                
        }
        catch(MqttException e){
            //logger.info("Exception initializing the mqtt client connection: " + e.toString());
            System.out.println("Exception initializing the mqtt client connection: " + e.toString());
        }
        
//        while(!mqttClient.isConnected()){
//            System.out.println("Not connected to Wi-Fi/broker!");
//            Thread.sleep(1000);//wait 1 sec
//            mqttClientConnect();
//
//        }
        mqttClientConnect();
        
        gpio = GpioFactory.getInstance();
        
        zonePins = new ArrayList();
        
        //Assign zones to pins
        zonePins.add(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00,PinState.HIGH)); //Zone 1
        zonePins.add(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01,PinState.HIGH)); //Zone 2
        zonePins.add(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02,PinState.HIGH)); //Zone 3
        zonePins.add(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03,PinState.HIGH)); //etc...
        zonePins.add(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04,PinState.HIGH)); //5
        zonePins.add(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05,PinState.HIGH)); //6
        zonePins.add(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06,PinState.HIGH)); //7
        zonePins.add(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07,PinState.HIGH)); //8
        
    }
    
    
    static int i = 0;
    static void allOff(){
        i =0;
        //final String b;
        //int i = 1;
        //mqttClient.publish("home/irrigation/Zone",previous.getbytes[],0,false);
        zonePins.forEach((a) -> {
            a.setState(PinState.HIGH);
            String b = Integer.toString(i+1)+";Off";
//            zonePins.get(i).setState(PinState.LOW);
            try {
                mqttClient.publish("home/irrigation/ZoneState",b.getBytes(),0,false);
            } catch (MqttException ex) {
                Logger.getLogger(SSTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            //System.out.println(b);
            i++;
        });
    }
    //mqttClient.publish("home/irrigation/Zone",previous.getbytes[],0,false);
//    static void sendAllOff() throws MqttException{
//        
//        for(int k = 0; k <= array.length; k++){
//                mqttClient.publish("home/irrigation/ZoneState",Integer.toString(k)+":Off",0,false);
//            }
//    }

    private static void mqttClientConnect() {
        //Logger logger = LoggerFactory.getLogger(MqttControlledIO.class);

        if(mqttClient.isConnected()) return;

        try{
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            //logger.info("Connecting to broker: "+ MQTT_BROKER);
            System.out.println("Connecting to broker: "+ MQTT_BROKER);
            mqttClient.connect(connOpts);
            //logger.info("Connected");
            System.out.println("Connected");
            //logger.info("Subscribing to topic");
            System.out.println("Subscribing to topic");
            mqttClient.subscribe("home/irrigation/ZoneCommand");
            //mqttClient.subscribe("home/irrigation/ZoneState");
            //logger.info("Subscribed to topic");
            System.out.println("Subscribed to topic");
        } catch (MqttException e){
            //logger.info("Error connecting to MQTT Client: " + e.toString());
            System.out.println("Error connecting to MQTT Client: " + e.toString());
        }
    }
        
    static class mqttCallback implements MqttCallback {
        //Logger logger = LoggerFactory.getLogger(MqttControlledIO.class);
        
        @Override
        public void connectionLost(Throwable thrwbl) {
            mqttClientConnect();
        }
        
        String previous;
        int trie;
        String b;
        @Override
        public void messageArrived(String topic, MqttMessage mm) throws Exception {
            //logger.info("MQTT Message Received:");
            //logger.info("  topic  : " + topic); //topic
            //logger.info("  message : " + mm.toString()); //payload
            System.out.println("MQTT Message Received:");
            System.out.println("  topic  : " + topic);
            System.out.println("  message : " + mm.toString());
            
            String [] arrOfStr;
            
            arrOfStr = mm.toString().split(";", 2);
            
            
            
            int i;
            
            i = Integer.parseInt(arrOfStr[0]);
            
//            if(trie == 0){
//                mqttClient.publish("home/irrigation/Zone",previous.getBytes(),0,false);
//            }else{
//                trie++;
//            }
            if("home/irrigation/ZoneCommand".equals(topic)){
                if("On".equals(arrOfStr[1])){
                    //zonePins.get(i-1).setState(true);
                    allOff();
                    b = Integer.toString(i)+";On";
                    zonePins.get(i-1).setState(PinState.LOW);
                    mqttClient.publish("home/irrigation/ZoneState",b.getBytes(),2,true);
                    System.out.println(b);
                }else{
                    //zonePins.get(i-1).setState(false);
                    allOff();
                    b = Integer.toString(i)+";Off";
                    zonePins.get(i-1).setState(PinState.HIGH);
                    mqttClient.publish("home/irrigation/ZoneState",b.getBytes(),2,true);
                    System.out.println(i+":Off");
                }
            }
            //previous = arrOfStr[0]+";Off";
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
            System.out.println("Delivery Complete.");
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
            
    }
        
}
    
    

