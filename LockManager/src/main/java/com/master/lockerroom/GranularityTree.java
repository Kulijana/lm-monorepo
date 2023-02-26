package com.master.lockerroom;

import common.dto.LockRequest;
import common.dto.LockType;

import java.util.*;

public class GranularityTree implements LockManager {
    private GranularityNode root;

    public GranularityTree(){
        root = new GranularityNode(null);
        tid2LeafNodes = new HashMap<>();
    }

//    used for deleting from the map, preventing deep dives into trees, they should be deleted from leafs up
    private HashMap<String, List<GranularityNode>> tid2LeafNodes;


//    questions:
//    should we perhaps specify the request more precisely
//    include if write is an insert, update or delete
//    allow readability of an existing row if another one is just being inserted
//    or another one being deleted(deleting most likely can go for a specific row if that is the case)

//    allow reading of entire tables, which prevents any kind of writing(similar to how it works now)
//    main advantage, locking an entire resource at once
//    and lock manager being able to recognize this and prevent writes on sub-resources


//    dbids will be in form app/db/table/row
    public synchronized boolean lock(LockRequest lockRequest){
        String dbid = lockRequest.getDbid();
        String[] levels = dbid.split("/");
        LockType type = lockRecursive(root, lockRequest, levels, 0);
        return type != LockType.INVALID ? true : false;
    }


    private LockType lockRecursive(GranularityNode node, LockRequest request, String[] levels, int levelIndex) {
//        end of recursion
        if (levelIndex == levels.length) {
            var newLock = node.newLockResult(request.getType());
            if (newLock != LockType.INVALID) {
                String tid = request.getTid();
                node.setLock(tid, newLock);
                var tidLeafs = tid2LeafNodes.get(tid);
                if (tidLeafs == null) {
                    tid2LeafNodes.put(tid, new ArrayList<>(Arrays.asList(node)));
                } else {
                    tidLeafs.add(node);
                }
            }
            return newLock;
        }
//        below code executes when not at the end of recursion
        LockType requestedLock = request.getType() == LockType.EXCLUSIVE ? LockType.INTENT_EXCLUSIVE : LockType.INTENT_SHARED;
        LockType newLock = node.newLockResult(requestedLock);

        if (newLock == LockType.INVALID)
            return LockType.INVALID;

        String childDbId = levels[levelIndex];
        GranularityNode child = node.branches.get(childDbId);
        if (child == null) {
            child = new GranularityNode(node);
            node.branches.put(childDbId, child);
        }

        LockType childLock = lockRecursive(child, request, levels, levelIndex + 1);
        if (childLock == LockType.INVALID) {
            return LockType.INVALID;
        }
        node.setLock(request.getTid(), newLock);
        return newLock;
    }

    public boolean unlock(LockRequest request){
        String tid = request.getTid();
        var leafs = tid2LeafNodes.get(tid);
        if(leafs==null){
            return false;
        }
        for (var leaf : leafs){
            leaf.removeLock(tid);
        }
        return true;
    }

    private class GranularityNode {

        GranularityNode parent;

        //    saving all locks by all transactions in this node
        HashMap<String, LockType> tid2LockType;
        LockType currentLockType;

        //    string is identifier of the next level
        HashMap<String, GranularityNode> branches;

        public GranularityNode(GranularityNode parent){
            this.parent = parent;
            branches = new HashMap<>();
            tid2LockType = new HashMap<>();
        }

        public LockType newLockResult(LockType type){
            return LockType.applyNewLock(type, currentLockType);
        }

        public void setLock(String tid, LockType type){
            currentLockType = type;
            tid2LockType.put(tid, type);
        }


//        finds the new strongest lock once the given transaction releases its lock
        public void removeLock(String tid){
            tid2LockType.remove(tid);
            var newLock = tid2LockType.values().stream().max(new GranularityNode.LockTypeComparator());
            if(newLock.isPresent()){
                currentLockType = newLock.get();
            }else{
                currentLockType = LockType.UNLOCK;
            }
            if(parent!=null){
                parent.removeLock(tid);
            }
        }

        private class LockTypeComparator implements Comparator<LockType>{
            @Override
            public int compare(LockType o1, LockType o2) {
                return o1.compareTo(o2);
            }
        }
    }

}
