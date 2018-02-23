package DownloadManager;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

class DownloadsTableModel extends AbstractTableModel implements Observer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1656683418638125584L;

	//These are the names for the tables's columns.
	private static final String[] columnNames = {"URL","Size","Progress","Status"};

	//These are the classes for each column's values.
	private static final Class[] columnClasses ={String.class,String.class,JProgressBar.class,String.class};

	//The tables's list of download.
	private ArrayList<Download> downloadList = new ArrayList<Download>();

	//Add new download to the table.
	public void addDownload(Download download)
	{
	//Register to be notified when the download changes.
	download.addObserver(this);
	downloadList.add(download);
	
	//Fire table row insertion notification to table.
	fireTableRowsInserted(getRowCount() -1,getRowCount() -1);
	}
	public Download getDownload(int row)
	{
	return downloadList.get(row);
	}
	public void clearDownload(int row)
	{
	downloadList.remove(row);
	fireTableRowsDeleted(row,row);
	}
	public int getColumnCount()
	{
	return columnNames.length;
	}
	public String getColumnName(int col)
	{
	return columnNames[col];
	}
	public Class getColumnClass(int col)
	{
	return columnClasses[col];
	}
	public int getRowCount()
	{
	return downloadList.size();
	}
	//Get value for a specific row and column combination.
	public Object getValueAt(int row,int col)
	{
	Download download = downloadList.get(row);
		switch(col)
		{
		case 0: //URL
		return download.getUrl();
		case 1: //Size
		int size = download.getSize();
		return (size == -1) ? "": Integer.toString(size);
		case 2: //Progress
		return new Float(download .getProgress());
		case 3: //Status
		return Download.STATUSES[download.getStatus()];
		}
	return "";
	}
	// Update is called when a download notifies its observers of any changes
	public void update(Observable o,Object arg)
	{
	int index = downloadList.indexOf(o);
	fireTableRowsUpdated(index,index);
	}
}