package oneway.sim;

import java.io.*;
import java.net.*;

class HTTPServer {

	private ServerSocket sock;
	private Socket connection;
	private int port;

	public HTTPServer() throws Exception
	{
		sock = new ServerSocket();
		sock.bind(null);
		if (!sock.isBound())
			throw new Exception("Invalid HTTP server port");
		port = sock.getLocalPort();
	}

	public int port() { return port; }

	public void close() throws Exception { sock.close(); }

	private String request(int timeout) throws Exception
	{
		if (timeout == 0)
			connection = sock.accept();
		else {
			sock.setSoTimeout(timeout);
			try {
				connection = sock.accept();
			} catch (SocketTimeoutException e) {
				sock.setSoTimeout(0);
				return null;
			}
			sock.setSoTimeout(0);
		}
		InputStream in = connection.getInputStream();
		int size = 0, cap = 4096, item;
		byte[] data = new byte[cap];
		while ((item = in.read()) != -1) {
			data[size++] = (byte) item;
			if (size > 3 &&
			    data[size - 4] == '\r' &&
			    data[size - 3] == '\n' &&
			    data[size - 2] == '\r' &&
			    data[size - 1] == '\n')
				break;
			if (size == cap) {
				byte[] newdata = new byte[cap + cap];
				for (int i = 0 ; i != cap ; ++i)
					newdata[i] = data[i];
				data = newdata;
				cap += cap;
			}
		}
		int rsize = -1;
		for (int i = 0 ; rsize < 0 && i != size ; ++i)
			if (data[i] == '\r')
				rsize = i;
        // abnormal request, ignore
		if (rsize < 0)
            return null;
        //throw new Exception("Invalid HTTP request");
		String requestLine = new String(data, 0, rsize);
		System.err.println(requestLine);
		String[] requestParts = requestLine.split(" ");
		String method = requestParts[0];
		String path = requestParts[1];
		if (!method.equals("GET"))
			throw new Exception("Invalid HTTP method");
		if (!path.startsWith("/"))
			throw new Exception("Invalid HTTP service");
		return path.substring(1);
	}

	private boolean isFileRequest(String path)
	{
		return path.endsWith(".png") || path.endsWith(".ico") || path.endsWith(".jpg");
	}

	private void replyFile(String path) throws Exception
	{
		path = path.replace('/', File.separatorChar);
		File file = new File(path);
		if (!file.exists()) {
			System.err.println("File \"" + path + "\" not found");
			return;
		}
		System.err.println("File \"" + path + "\" found and sent");
        //		FileInputStream in = new FileInputStream(file);


		// String head = "HTTP/1.0 200 OK\r\n";
        // head += "Content-type: " + "image/png" + "\r\n";
		// head += "Content-Length: " + file.length() + "\r\n";
		// OutputStream out = connection.getOutputStream();
		// out.write(head.getBytes());
		// byte[] buf = new byte [4096]; int i;
		// while ((i = in.read(buf)) > 0)
		// 	out.write(buf, 0, i);
		// in.close();

        PrintWriter out = new PrintWriter(connection.getOutputStream());
        BufferedOutputStream dataOut = new BufferedOutputStream(connection.getOutputStream());

        int fileLength = (int)file.length();
        byte[] fileData = new byte[fileLength];
        FileInputStream fileIn = new FileInputStream(file);
        fileIn.read(fileData);
        fileIn.close();

        //send HTTP headers
        out.println("HTTP/1.0 200 OK");
        out.println("Server: Java HTTP Server 1.0");
        //      out.println("Content-type: " + "image/png");
        out.println("Content-length: " + file.length());
        out.println(); //blank line between headers and content
        out.flush(); //flush character output stream buffer

        dataOut.write(fileData,0,fileLength); //write file
        dataOut.flush(); //flush binary output stream buffer
        
        out.close();
        dataOut.close();
		connection.close();
	}

	public char nextRequest(int timeout) throws Exception
	{
		String requestPath = request(timeout);
		if (requestPath == null)
			return 'X';
		if (isFileRequest(requestPath)) {
			replyFile(requestPath);
			return 'I';
		}
		if (requestPath.equals("play"))
			return 'P';
		if (requestPath.equals("step"))
			return 'N';
		if (requestPath.equals("stop"))
			return 'S';
		if (requestPath.equals(""))
			return 'B';
		throw new Exception("Invalid request");
	}

	public void replyState(String content, long refresh) throws Exception
	{
		OutputStream out = connection.getOutputStream();
		String head = "HTTP/1.0 200 OK\r\n";
		if (refresh > 0) head += "Refresh: " + refresh + "; url=http://localhost:" + port + "/step\r\n";
		head += "Content-Length: " + content.length() + "\r\n\r\n";
		out.write(head.getBytes());
		out.write(content.getBytes());
		connection.close();
	}
}
