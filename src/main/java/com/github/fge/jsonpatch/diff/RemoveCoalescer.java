package com.github.fge.jsonpatch.diff;

import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by joel.tucci on 3/20/14.
 */
final class RemoveCoalescer {

    private RemoveCoalescer() {
    }

    private static int calculateBlockRemoveLength(int originalDiffListIndex,List<Diff> diffs) {
        //seek ahead to find out where next instruction begins
        Diff curDiff=diffs.get(originalDiffListIndex);
        int seekIndex=originalDiffListIndex+1;
        while(seekIndex<diffs.size() && diffs.get(seekIndex).arrayPath.equals(curDiff.arrayPath) &&
                diffs.get(seekIndex).operation == DiffOperation.REMOVE) {
            seekIndex++;
        }
        return seekIndex-originalDiffListIndex;
    }

    private static Diff condenseRemovesToBatchCommand(List<Diff> diffs,int removeBlockLength,int startIndex) {
        List<Integer> removedIndices=Lists.newArrayList();
        int curOffset=0;  //Needed to map between the current remove indices and the original array
        Diff curDiff=diffs.get(startIndex);
        removedIndices.add(curDiff.firstArrayIndex);
        Integer previousIndex;
        for(int blockRemoveIndex=1;blockRemoveIndex<removeBlockLength;blockRemoveIndex++) {
            previousIndex=curDiff.firstArrayIndex;
            curDiff=diffs.get(blockRemoveIndex+startIndex);
            if(curDiff.firstArrayIndex == previousIndex)
                curOffset++;
            removedIndices.add(curDiff.firstArrayIndex+curOffset);
        }
        return Diff.batchRemove(diffs.get(startIndex).arrayPath, removedIndices);

    }

    /**
     * Combine multiple remove commands on an array into a single batch_remove.
     * This not only results in more compact JSON, it also makes diffs easier to understand and create
     * The value in the batch remove correspond to the values in the original array, and do not
     * have to either be specified in reverse order or require difficult calculations to figure out which nodes
     * are actually getting removed
     * @param diffs
     */
    public static void coalesceRemoves(final List<Diff> diffs) {

        //I am assuming that the previous step emits the removes in order, i.e. all removes
        //for a given array are consecutive.  This is how the current factorizediffs implementation works
        //obviously if this assumption no longer holds then this part will no longer optimize the removes

        final List<Diff> coalescedDiffs = Lists.newArrayList();
        for(int i=0;i<diffs.size();i++) {

            Diff curDiff=diffs.get(i);
            if(curDiff.operation != DiffOperation.REMOVE) {
                coalescedDiffs.add(curDiff);
                continue; //Only coalesce remove operations
            }

            if(curDiff.arrayPath == null) {
                coalescedDiffs.add(curDiff);
                continue; //not an array
            }

            int removeBlockLength=calculateBlockRemoveLength(i,diffs);
            if(removeBlockLength == 1) {
                //Single remove, so just add it directly
                coalescedDiffs.add(diffs.get(i));
                continue;
            }

            coalescedDiffs.add(condenseRemovesToBatchCommand(diffs,removeBlockLength,i));
            //Now that we have condensed all these remove commands, we need to skip ahead to the next operation
            i=i+removeBlockLength-1;

        }

        diffs.clear();
        diffs.addAll(coalescedDiffs);
    }

}
