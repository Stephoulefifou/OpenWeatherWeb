package ch.hearc.heg.scl.rmiObj;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import ch.hearc.heg.scl.business.ResultSearch;
import ch.hearc.heg.scl.business.StationMeteo;

public interface IOpenWeatherServices extends Remote {
    ResultSearch getWeatherByCoordinates(double latitude, double longitude) throws RemoteException;
    void refreshAllStations() throws RemoteException;
    ArrayList<StationMeteo> getStations() throws RemoteException;
    ResultSearch getWeatherForStation(int stationId) throws RemoteException;
}
