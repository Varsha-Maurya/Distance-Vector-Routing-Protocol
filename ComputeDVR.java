import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

/**
 * Class for ComputeDVR
 */
public class ComputeDVR {
	

	public Router myNode;
	public int round;
	public static long frequency = 15000;


  /**
   *  ComputeDVR constructor.
   * @param myNode - Node for the {@link Router} Router
   */
	public ComputeDVR(Router myNode) {
		this.myNode = myNode;
		this.round = 1;
	}
	
	public static void main(String[] args) throws Exception {

		//Check the input arguments
    if (args.length != 1) {
      System.out.println("You need to have the router connectivity file as an argument");
      return;
    }

		String filename = args[0];
    //Get the router name
		String myRouter = filename.split(".dat")[0];
		Router myNode = new Router(myRouter);
		ComputeDVR vector = new ComputeDVR(myNode);
		long currTime = System.currentTimeMillis();
		
		try {
			int Port = 9000 + (int) myRouter.charAt(0);
			DatagramSocket Client = new DatagramSocket(Port);
			InetAddress IP = InetAddress.getByName("localhost");

			while(true){
				if(vector.myNode.routeChange){
					try{
						BufferedReader reader = new BufferedReader(new FileReader(filename));
						// the first line is the number of neighbors
						String page = reader.readLine();
						Map<String, Double> nextNode = new HashMap<>();
						//this is the neighbors.
            if (getNeigbhors(reader, nextNode)) {
              return;
            }
            reader.close();

						Set<String> addedLink = new HashSet<>();
						addedLink.addAll(nextNode.keySet());
						addedLink.addAll(vector.myNode.nextNode.keySet());

						for(String nodeDetail : addedLink){
							Double distance = nextNode.get(nodeDetail);

							if(distance == null){
								distance = Double.POSITIVE_INFINITY;
							}

							vector.myNode.newNode(nodeDetail, distance, nodeDetail, vector.round == 1);
						}

					}
					catch(FileNotFoundException e){
						System.err.println("File does not exist");
						return;
					}
					catch(NumberFormatException e){
						System.err.println("Data is invalid");
						return;
					}
					catch(IOException e){
						System.err.println("No valid data!");
						return;
					}

					System.out.println("output number " + vector.round++);
					System.out.println(vector.myNode+"\n");

					for(String closeLink : vector.myNode.nextNode.keySet()){

						if(vector.myNode.nextNode.get(closeLink) == Double.POSITIVE_INFINITY){
							continue;
						}
						String linkCost = myNode.updateLinkData(closeLink);
						byte[] data = linkCost.getBytes();
						int ClientPort = 9000 + (int) closeLink.charAt(0);
						DatagramPacket dataPacket = new DatagramPacket(data, data.length, IP, ClientPort);
						Client.send(dataPacket);
					}
					vector.myNode.broadcast();
				}
        currTime = getCurrTime(vector, currTime, Client);
      }
		}
		catch (SocketException e) {
      System.err.println("Socket Exception!");
			e.printStackTrace();
		}  catch (UnknownHostException e) {
      System.err.println("Unknown Host Exception!");
			e.printStackTrace();
		}

	}

  private static boolean getNeigbhors(BufferedReader reader, Map<String, Double> nextNode)
      throws IOException {
    String page;
    while((page = reader.readLine()) != null) {
      StringTokenizer token = new StringTokenizer(page);
      if(token.countTokens() != 2){
        System.err.println("Please provide valid data");
        reader.close();
        return true;
      }
      String nodeDetail = token.nextToken().trim();
      Double distance = Double.parseDouble(token.nextToken().trim());
      nextNode.put(nodeDetail, distance);
    }
    return false;
  }



  /**
   * Method used to calulate the delta between two sends.
   * @param vector - Send the compute vector.
   * @param currTime - Send the current time
   * @param client - Send the {@link DatagramSocket} socket.
   */

  private static long getCurrTime(ComputeDVR vector, long currTime, DatagramSocket client)
      throws IOException {
    try{

      long TimeOut = frequency - (System.currentTimeMillis() - currTime);

      if (TimeOut < 0) {
        throw new SocketTimeoutException();
      }

      byte[] DataPack = new byte[1024];
      DatagramPacket PacketData = new DatagramPacket(DataPack, DataPack.length);

      client.setSoTimeout((int) TimeOut);

      client.receive(PacketData);
      byte[] nodeData = PacketData.getData();

      Router routerData = new Router(nodeData);

      for(String node : routerData.linkCost.keySet()){

        costCal linkValue = vector.myNode.linkCost.get(node);

        Double CostOfLink = Double.POSITIVE_INFINITY;
        String count = null;

        if(linkValue != null){
          CostOfLink = linkValue.cost;
          count = linkValue.cal;
        }

        Double DVR = routerData.linkCost.get(node).cost + vector.myNode.nextNode.get(routerData.node);


        if(CostOfLink > DVR){
          vector.myNode.updateLinkCost(node, DVR, routerData.node);
        } else if(count != null && count.equals(routerData.node) && !CostOfLink.equals(DVR)){
          vector.myNode.updateLinkCost(node, DVR, routerData.node);
        }
      }
    } catch(SocketTimeoutException e){
      vector.myNode.routeChange = true;
      currTime = System.currentTimeMillis();
    }
    return currTime;
  }

}
