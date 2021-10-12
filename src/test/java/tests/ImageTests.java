package tests;

import dto.PostImageResponse;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.MultiPartSpecification;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageTests extends BaseTest {
    String IMAGE_UPLOAD_URL = "/upload";
    String IMAGE_DELETE_URL = "/image/{imageHash}";
    String IMAGE_FAVORITE_URL = "/image/{imageHash}/favorite";
    String IMAGE_GET_URL = "/image/{imageHash}";

    String EXISTING_IMAGE_HASH = "JoZqQmY";

    private final String PATH_TO_IMAGE = "src/test/resources/test.jpg";
    static String encodedImage;
    String uploadedImageId;
    MultiPartSpecification base64MultiPartSpec;
    MultiPartSpecification multiPartSpecificationFile;
    RequestSpecification requestSpecificationWithAuthAndMultipartImage;
    RequestSpecification requestSpecificationWithAuthAndBase64;
    ResponseSpecification responseSpecificationWith400Error;
    ResponseSpecification responseSpecificationSetImageUpload;

    @BeforeEach
    void beforeTest() {
        byte[] byteArray = getFileContent();
        encodedImage = Base64.getEncoder().encodeToString(byteArray);

        base64MultiPartSpec = new MultiPartSpecBuilder(encodedImage)
                .controlName("image")
                .build();

        multiPartSpecificationFile = new MultiPartSpecBuilder(new File(PATH_TO_IMAGE))
                .controlName("image")
                .build();

        requestSpecificationWithAuthAndMultipartImage = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecificationWithAuth)
                .addFormParam("type", "jpeg")
                .addMultiPart(multiPartSpecificationFile)
                .build();

        requestSpecificationWithAuthAndBase64 = new RequestSpecBuilder()
                .addRequestSpecification(requestSpecificationWithAuth)
                .addFormParam("type", "base64")
                .addMultiPart(base64MultiPartSpec)
                .build();

        responseSpecificationWith400Error = new ResponseSpecBuilder()
                .expectStatusCode(400)
                .expectBody("success", is(false))
                .expectBody("data.error", is("Bad Request"))
                .build();

        responseSpecificationSetImageUpload = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectBody("success", is(true))
                .expectBody("data.type", is("image/jpeg"))
                .build();

    }

    @Test
    void uploadFileTest() {
        uploadedImageId = given()
                .spec(requestSpecificationWithAuthAndMultipartImage)
                .expect()
                .spec(responseSpecificationSetImageUpload)
                .when()
                .post(IMAGE_UPLOAD_URL)
                .prettyPeek()
                .then()
                .extract()
                .body()
                .as(PostImageResponse.class)
                .getData().getDeletehash();
    }

    @Test
    void emptyTitleUploadFileTest() {
        uploadedImageId = given()
                .spec(requestSpecificationWithAuthAndMultipartImage)
                .formParam("title", "")
                .expect()
                .spec(responseSpecificationSetImageUpload)
                .body("data.title", is(nullValue()))
                .when()
                .post(IMAGE_UPLOAD_URL)
                .prettyPeek()
                .then()
                .extract()
                .body()
                .as(PostImageResponse.class)
                .getData().getDeletehash();
    }

    @Test
    void emptyNameUploadFileTest() {
        uploadedImageId = given()
                .spec(requestSpecificationWithAuthAndMultipartImage)
                .formParam("name", "")
                .expect()
                .body("data.name", is(""))
                .spec(responseSpecificationSetImageUpload)
                .when()
                .post(IMAGE_UPLOAD_URL)
                .prettyPeek()
                .then()
                .extract()
                .body()
                .as(PostImageResponse.class)
                .getData().getDeletehash();
    }

    @Test
    void uploadNoImageTest() {
        given()
                .spec(requestSpecificationWithAuth)
                .expect()
                .spec(responseSpecificationWith400Error)
                .when()
                .post(IMAGE_UPLOAD_URL)
                .prettyPeek();
    }

    @Test
    void imageBase64UploadFileTest() {
        uploadedImageId = given()
                .spec(requestSpecificationWithAuthAndBase64)
                .expect()
                .spec(responseSpecificationSetImageUpload)
                .when()
                .post(IMAGE_UPLOAD_URL)
                .prettyPeek()
                .then()
                .extract()
                .body()
                .as(PostImageResponse.class)
                .getData().getDeletehash();
    }

    @Test
    void imageWrongTypeUploadFileTest() {
        given()
                .spec(requestSpecificationWithAuthAndBase64)
                .formParam("type", "jpeg")
                .expect()
                .spec(responseSpecificationWith400Error)
                .when()
                .post(IMAGE_UPLOAD_URL)
                .prettyPeek();

    }

    @Test
    void getImageTest() {
        given()
                .spec(requestSpecificationWithAuth)
                .when()
                .get(IMAGE_GET_URL, existingImageHash)
                .prettyPeek()
                .then()
                .spec(positiveResponseSpecification)
                .body("data.id", is(EXISTING_IMAGE_HASH));
    }

    @Test
    void favoriteAnImageTest() {
        Response response = given()
                .spec(requestSpecificationWithAuth)
                .when()
                .post(IMAGE_FAVORITE_URL, existingImageHash)
                .prettyPeek();

        assertEquals(200, response.statusCode());
        if (response.jsonPath().getString("data").equals("favorited")) {
            assertEquals("favorited", response.jsonPath().getString("data"));
        } else {
            assertEquals("unfavorited", response.jsonPath().getString("data"));
        }
    }


    @Test
    void unexistingHashFavoriteTest() {
        Response response = given()
                .spec(requestSpecificationWithAuth)
                .when()
                .post(IMAGE_FAVORITE_URL, "test")
                .prettyPeek();

        assertEquals(404, response.statusCode());
    }

    @Test
    void allEmptyUploadFileTest() {
        given()
                .spec(requestSpecificationWithAuth)
                .expect()
                .spec(responseSpecificationWith400Error)
                .when()
                .post(IMAGE_UPLOAD_URL)
                .prettyPeek();
    }


    @AfterEach
    void tearDown() {
        if (uploadedImageId != null) {
            given()
                    .spec(requestSpecificationWithAuth)
                    .when()
                    .delete(IMAGE_DELETE_URL, uploadedImageId)
                    .prettyPeek()
                    .then()
                    .spec(positiveResponseSpecification);
        }
    }

    private byte[] getFileContent() {
        byte[] bytes = new byte[0];
        try {
            bytes = FileUtils.readFileToByteArray(new File((PATH_TO_IMAGE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
