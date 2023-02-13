package com.master.lockerroom;

import java.util.HashMap;

public class GranularityNode {

    boolean finalNode;
//    string is identifier of the next level
    HashMap<String, GranularityNode> branches;
//    possibly add some connection to the lock

    public GranularityNode(){
        finalNode = false;
        branches = new HashMap<>();
    }
}
