package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jackson.JacksonUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joel.tucci on 3/20/14.
 */
public class BatchRemoveOperationTest extends JsonPatchOperationTest {

    protected BatchRemoveOperationTest(String prefix) throws IOException {
        super(BatchRemoveOperation.OPERATION_NAME);
    }


}

