package com.github.fge.jsonpatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.ArrayNode;

import com.fasterxml.jackson.databind.node.MissingNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by joel.tucci on 3/20/14.
 */
public class BatchRemoveOperation extends PathValueOperation {

    public static final String OPERATION_NAME="batch_remove";

    @JsonCreator
    public BatchRemoveOperation(@JsonProperty("path") final JsonPointer path,
                            @JsonProperty("value") final JsonNode value)
    {
        super(OPERATION_NAME, path, value);
    }

    private void validateDataTypes(JsonNode node) throws JsonPatchException{

        JsonNode parentNode=path.parent().path(node);
        if (parentNode.isMissingNode())
            throw new JsonPatchException(BUNDLE.getMessage(
                    "jsonPatch.noSuchPath"));

        if(!value.isArray())
            throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.valueNotArray"));
        if(!parentNode.isArray())
            throw new JsonPatchException(BUNDLE.getMessage("jsonPatch.nodeNotArray"));
    }

    @Override
    public JsonNode apply(JsonNode node) throws JsonPatchException {
        if (path.isEmpty())
            return MissingNode.getInstance();

        validateDataTypes(node);

        //The simplest, if perhaps naive implementation is to do a reverse sort on the indices to be deleted and then
        //delete them in that order.  This is O(n*m)+O(m lg m)(where n is the length of the list and m is the # of nodes to be deleted
        //but it can be done without copying the entire original array
        ArrayNode valArray=(ArrayNode) value;
        final JsonNode ret=node.deepCopy();
        final ArrayNode targetArray = (ArrayNode) path.parent().get(ret);


        List<Integer> victimIndices=new ArrayList<Integer>();
        for(int i=0;i<valArray.size();i++) {
            Integer victimIndex=valArray.get(i).asInt();
            if(victimIndex >= targetArray.size())
                throw new JsonPatchException(BUNDLE.getMessage(
                        "jsonPatch.indexDoesNotExist"));
            victimIndices.add(victimIndex);
        }
        Collections.sort(victimIndices);

        for(int i=victimIndices.size()-1;i>=0;i--) {
            targetArray.remove(victimIndices.get(i));
        }


        return ret;
    }

}
