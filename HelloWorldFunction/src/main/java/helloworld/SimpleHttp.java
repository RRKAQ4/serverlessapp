package helloworld;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.*;

public class SimpleHttp implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        //System.out.println("ID PARM : " + input.getQueryStringParameters().get("ID"));
        String output = "Empty!!";
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        System.out.println("Getting Table Reference..");
        Table table = dynamoDB.getTable("ProductCatalog");

        // Build a list of related items
        List<Number> relatedItems = new ArrayList<>();
        relatedItems.add(341);
        relatedItems.add(472);
        relatedItems.add(649);

        // Build a map of product pictures
        Map<String, String> pictures = new HashMap<>();
        pictures.put("FrontView", "http://example.com/products/123_front.jpg");
        pictures.put("RearView", "http://example.com/products/123_rear.jpg");
        pictures.put("SideView", "http://example.com/products/123_left_side.jpg");

        // Build a map of product reviews
        Map<String, List<String>> reviews = new HashMap<>();

        List<String> fiveStarReviews = new ArrayList<>();
        fiveStarReviews.add("Excellent! Can't recommend it highly enough!  Buy it!");
        fiveStarReviews.add("Do yourself a favor and buy this");
        reviews.put("FiveStar", fiveStarReviews);

        List<String> oneStarReviews = new ArrayList<>();
        oneStarReviews.add("Terrible product!  Do not buy this. Worst.");
        reviews.put("OneStar", oneStarReviews);

        System.out.println("Build the item..");
        // Build the item
        Item item = new Item().withPrimaryKey("Id", Integer.parseInt(input.getQueryStringParameters().get("ID"))).withString("Title", "Bicycle 123")
                .withString("Description", "123 description").withString("BicycleType", "Hybrid")
                .withString("Brand", "Brand-Company C").withNumber("Price", 500)
                .withStringSet("Color", new HashSet<>(Arrays.asList("Blue", "Black", "Pink")))
                .withString("ProductCategory", "Bicycle").withBoolean("InStock", true).withNull("QuantityOnHand")
                .withList("RelatedItems", relatedItems).withMap("Pictures", pictures).withMap("Reviews", reviews).withString("AgeLimit", "18-28");

        // Write the item to the table
        // PutItemOutcome outcome = table.putItem(item);

        try {
            /* Send Put Item Request */
            System.out.println("Trying to put items..");
            PutItemOutcome result = table.putItem(item);

            System.out.println("Status Code : " + result.getPutItemResult().getSdkHttpMetadata().getHttpStatusCode());

            /* Printing Old Attributes Name and Values */
            if (result.getPutItemResult().getAttributes() != null) {
                result.getPutItemResult().getAttributes().forEach((key, value) -> System.out.println(key + " " + value));
            } else {
                System.out.println("New Item Added to DynamoDB Table : " + table.getTableName());
                output = "{ \"New Item Added to DynamoDB Table : \" }" + table.getTableName();
            }

        } catch (AmazonServiceException e) {

            System.out.println("Caught exception..");
            System.out.println(e.getErrorMessage());

        }

        return response
                .withStatusCode(200)
                .withBody(output);
    }
}
