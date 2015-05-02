/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\javier\\workspace\\OpenStreetMapViewer\\src\\team\\osmviewer\\services\\GpsInterface.aidl
 */
package team.osmviewer.services;
public interface GpsInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements team.osmviewer.services.GpsInterface
{
private static final java.lang.String DESCRIPTOR = "team.osmviewer.services.GpsInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an team.osmviewer.services.GpsInterface interface,
 * generating a proxy if needed.
 */
public static team.osmviewer.services.GpsInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof team.osmviewer.services.GpsInterface))) {
return ((team.osmviewer.services.GpsInterface)iin);
}
return new team.osmviewer.services.GpsInterface.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements team.osmviewer.services.GpsInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
}
}
}
