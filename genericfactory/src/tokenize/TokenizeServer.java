package tokenize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import patterns.ContainerGranularity;

public class TokenizeServer {



	/* CAB - kind of an abuse to pass the application to this thing, but the TestParser
	 * method is in Application and we don't want to duplicate code
	 */
	Application application;
	public TokenizeServer(Application application) {
		this.application = application;
	}

	/* start up the server on the specified socket and listen and fulfill requests
	 * until a connection sends the string "quit"
	 */
	boolean done;
	public void serveForever(int port) {
		done = false;
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while (!done)
			{
				handleClient(serverSocket.accept());
			}
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/* a request should be of the form:
	 * filename:pattern\n
	 * 
	 * the response will be a number of lines from the visitor of the form:
	 * line number:info
	 * 
	 * ending with an empty line
	 */
	private void handleClient(Socket clientSocket) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		Writer out = new PrintWriter(clientSocket.getOutputStream(), true);

		while (!clientSocket.isClosed()) {
			String request = reader.readLine();
			System.out.println("Got request: " + request);
			if (request == null) {
				break;
			}
			if (request.toLowerCase().startsWith("quit")) {
				done = true;
			} else if (request.toLowerCase().startsWith("done")) {
				break;
			}
			else
			{
				/* if a single request fails, then that's no reason to actually fail
				 * so send ERROR if something fails, print out the stack trace,
				 * and return to serve more requests
				 */
				try
				{
					String parts[] = request.trim().split(":");
					// parts can now contain a container argument
					assert(parts.length >= 2 && parts.length <= 3);
					String filepath = parts[0];
					String pattern = parts[1];
					ContainerGranularity containerGranularity = ContainerGranularity.FULL;
					if (parts.length > 2) {
						String args = parts[2];
						String argParts[] = args.split("=");
						if (argParts[0].trim().toLowerCase().equals("containergranularity")) {
							String gran = argParts[1].trim().toLowerCase();
							if (gran.equals("full")) {
								containerGranularity = ContainerGranularity.FULL;
							} else if (gran.equals("name_only")) {
								containerGranularity = ContainerGranularity.NAME_ONLY;
							} else {
								throw new RuntimeException("don't know how to handle Container " +
										"Granularity" + gran + ". Valid values are 'full' and " +
								"'name_only'.");
							}
						}
					}
					application.ParserTest(filepath, pattern, containerGranularity, out);
					System.out.println("Done with request");
					out.write("\n\n\n");
					out.flush();
				} catch (Exception e)
				{
					out.write("ERROR parsing " + request);
					e.printStackTrace(new PrintWriter(out));
					out.write("\n\n\n");
				}
			}
		}
		out.close();
		reader.close();
	}


}
