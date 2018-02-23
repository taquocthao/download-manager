package DownloadManager;
import java.io.*;
import java.net.URISyntaxException;
//import java.net.*;
import java.net.URL;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FilenameUtils;

class Download extends Observable implements Runnable
{
	private static final int MAX_BUFFER_SIZE=1024;
	public static final String STATUSES[]={"Downloading","Paused","Complete","Canceled","Error"};
	
	public static final int DOWNLOADING=0;
	public static final int PAUSED=1;
	public static final int COMPLETE=2;
	public static final int CANCELED=3;
	public static final int ERROR=4;
	
	private URL url;
	private int size;
	private int downloaded;
	private int status;
	
	public Download(URL url)
	{
	this.url=url;
	size=-1;
	downloaded=0;
	status=DOWNLOADING;
	download();
	}
	public String getUrl()
	{
	return url.toString();
	}
	public int getSize()
	{
	return size;
	}
	public float getProgress()
	{
	return ((float)downloaded/size)*100;
	}
	public int getStatus()
	{
	return status;
	}
	public void pause()
	{
	status=PAUSED;
	stateChanged();
	}
	public void resume()
	{
	status=DOWNLOADING;
	stateChanged();
	download();
	}
	public void cancel()
	{
	status = CANCELED;
	stateChanged();
	}
	private void error()
	{
	status=ERROR;
	stateChanged();
	}
	private void download()
	{
	Thread thread = new Thread(this);
	thread.start();
	}
	private String getFileName(URL url) throws URISyntaxException
	{	
		String fileName = FilenameUtils.getName(url.getPath());
		
		return fileName.substring(fileName.lastIndexOf('/')+1, fileName.length());
	}
	public void run()
	{
	RandomAccessFile file=null;
	InputStream stream=null;
		try
		{
		HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
		connection.setRequestProperty("Range","bytes= " +downloaded+"-");
		connection.connect();
			if(connection.getResponseCode()/100 !=2)
			{
			error();
			}
		int contentLength = connection.getContentLength();
			if(contentLength<1)
			{
			error();
			}
			if(size==-1)
			{	
			size=contentLength;
			stateChanged();
			}
		file = new RandomAccessFile(getFileName(url),"rws");
		file.seek(downloaded);
		
		stream = connection.getInputStream();
			while(status==DOWNLOADING)
			{
			byte buffer[];
				if(size-downloaded>MAX_BUFFER_SIZE)
				{
				buffer =new byte[MAX_BUFFER_SIZE];
				}
				else
				{
				buffer =new byte[size - downloaded];
				}
			int read = stream.read(buffer);
				if(read ==-1)
				break;
			file.write(buffer,0,read);
			downloaded += read;
			stateChanged();
			}
			if(status==DOWNLOADING)
			{
			status=COMPLETE;
			stateChanged();
			}
		}
		catch(Exception e)
		{
		error();
		}
		finally
		{
			if(file != null)
			{
				try
				{
				file.close();
				}
				catch(Exception e){}
			}
			if(stream != null)
			{
				try
				{
				stream.close();
				}
				catch(Exception e)
				{}
			}
		}
	}
	private void stateChanged()
	{
		setChanged();
		notifyObservers();
	}
}