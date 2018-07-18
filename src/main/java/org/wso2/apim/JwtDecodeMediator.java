package org.wso2.apim;

import org.apache.commons.codec.binary.Base64;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jwt Decode Mediator Implementation.
 */
public class JwtDecodeMediator extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(JwtDecodeMediator.class);
    private static JSONObject accountIdsJson = new JSONObject();
    private String jwtHeader;
    private String accountIds;

    private static String retrieveAccountId(String accountRequestInfo) {
        String[] split_string = accountRequestInfo.split("\\.");
        String base64EncodedBody = split_string[1];

        Base64 base64 = new Base64();
        try {
            String decodedString = new String(base64.decode(base64EncodedBody.getBytes()));
            JSONParser parser = new JSONParser();
            JSONObject accountRequestInfoJson = (JSONObject) parser.parse(decodedString);
            if (accountRequestInfoJson.containsKey("accountRequestIds")) {
                JSONArray accountRequestIdsArray = (JSONArray) accountRequestInfoJson.get("accountRequestIds");
                String[] accountIdsArray = new String[accountRequestIdsArray.size()];
                JSONObject accountRequestId;
                JSONArray accountIds = new JSONArray();
                for (int i = 0; i < accountRequestIdsArray.size(); i++) {
                    accountRequestId = (JSONObject) accountRequestIdsArray.get(i);
                    accountIdsArray[i] = accountRequestId.get("accountId").toString();
                    accountIdsArray[i] = accountIdsArray[i].split("\"")[1].split("\"")[0];
                    JSONObject accountId = new JSONObject();
                    accountId.put("AccountId", accountIdsArray[i]);
                    accountIds.add(accountId);
                }
                accountIdsJson.put("data", accountIds);
                String transformedJson = accountIdsJson.toString();
                return transformedJson;
            } else {
                if (log.isDebugEnabled()) log.error("Account Request Ids is not available");
            }
        } catch (ParseException e) {
            log.error("Error in passing Account-Request-Information " + e.toString());
        }
        return null;
    }

    @Override
    public boolean mediate(MessageContext context) {
        accountIds = retrieveAccountId(getJWT_HEADER());
        JsonUtil.newJsonPayload(((Axis2MessageContext) context).getAxis2MessageContext(), accountIds,
                true, true);
        context.setProperty("accountIds", accountIdsJson);
        log.info("--------------------ACCOUNT_IDs--------------------" + context.getProperty("accountIds").toString());
        return true;
    }

    public String getJWT_HEADER() {
        return jwtHeader;
    }

    public void setJWT_HEADER(String jwtHeader) {
        this.jwtHeader = jwtHeader;
    }

    public String getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(String accountIds) {
        this.accountIds = accountIds;
    }
}
