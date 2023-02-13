package com.master.lockerroom;

import common.dto.LockRequest;

public class GranularityTree {
    private GranularityNode root;

    public GranularityTree(){
        root = new GranularityNode();
    }

//    questions:
//    should we perhaps specify the request more precisely
//    include if write is an insert, update or delete
//    allow readability of an existing row if another one is just being inserted
//    or another one being deleted(deleting most likely can go for a specific row if that is the case)

//    allow reading of entire tables, which prevents any kind of writing(similar to how it works now)
//    main advantage, locking an entire resource at once
//    and lock manager being able to recognize this and prevent writes on sub-resources


    public void addLock(LockRequest lockRequest){
        String dbid = lockRequest.getDbid();
        String[] levels = dbid.split("/");
        var node = root;
        for(int i=0;i<levels.length;i++){
            var level = levels[i];
            boolean finalNode = i == levels.length - 1;
            var target = node.branches.get(level);
            if(target == null){
                node.branches.put(level, new GranularityNode());
                target = node.branches.get(level);
            }
//            if needed because we don't want to overwrite other final branches
            if(finalNode){
                target.finalNode = finalNode;
            }
        }
    }

    public void lockable(LockRequest lockRequest){
        String dbid = lockRequest.getDbid();
        String[] levels = dbid.split("/");
        var node = root;
    }

    private boolean isNodeLockable(GranularityNode node, String[] levels, int levelIndex){
        if(node == null){
            return true;
        }
        if(node.finalNode){
            return false;
        }
        var level = levels[levelIndex];

    }
}
