package net.skhu.firechat2.Room.MemberLocation;

public class LocationFunc {
    public static String locationDataStr(double latitude, double longitude){
        return "geo:"+latitude+", "+longitude;
    }
}
