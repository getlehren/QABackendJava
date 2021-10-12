package tests;

import io.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageTests extends BaseTest {
    private final String PATH_TO_IMAGE = "src/test/resources/test.jpg";
    static String encodedImage;
    String uploadedImageId;

    @BeforeEach
    void beforeTest() {
        byte[] byteArray = getFileContent();
        encodedImage = Base64.getEncoder().encodeToString(byteArray);
    }

    @Test
    void uploadFileTest() {
        uploadedImageId = given()
                .header("Authorization", token)
                .multiPart("image", new File(PATH_TO_IMAGE))
                .formParam("title", "ImageTitle")
                .formParam("name", "ImageName")
                .formParam("type", "jpeg")
                .formParam("description", "ImageDescription")
                .expect()
                .body("success", is(true))
                .body("data.id", is(notNullValue()))
                .body("data.type", is("image/jpeg"))
                .body("data.name", is("ImageName"))
                .body("data.description", is("ImageDescription"))
                .body("data.title", is("ImageTitle"))
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void emptyTitleUploadFileTest() {
        uploadedImageId = given()
                .header("Authorization", token)
                .multiPart("image", new File(PATH_TO_IMAGE))
                .formParam("title", "")
                .formParam("name", "ImageName")
                .formParam("type", "jpeg")
                .formParam("description", "ImageDescription")
                .expect()
                .body("success", is(true))
                .body("data.id", is(notNullValue()))
                .body("data.type", is("image/jpeg"))
                .body("data.name", is("ImageName"))
                .body("data.description", is("ImageDescription"))
                .body("data.title", is(nullValue()))
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void emptyNameUploadFileTest() {
        uploadedImageId = given()
                .header("Authorization", token)
                .multiPart("image", new File(PATH_TO_IMAGE))
                .formParam("title", "ImageTitle")
                .formParam("name", "")
                .formParam("type", "jpeg")
                .formParam("description", "ImageDescription")
                .expect()
                .body("success", is(true))
                .body("data.id", is(notNullValue()))
                .body("data.type", is("image/jpeg"))
                .body("data.name", is(""))
                .body("data.description", is("ImageDescription"))
                .body("data.title", is("ImageTitle"))
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void uploadNoImageTest() {
        uploadedImageId = given()
                .header("Authorization", token)
                .formParam("title", "ImageTitle")
                .formParam("name", "ImageName")
                .formParam("type", "jpeg")
                .formParam("description", "ImageDescription")
                .expect()
                .statusCode(400)
                .body("success", is(false))
                .body("data.error", is("Bad Request"))
                .body("data.request", is("/3/upload"))
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void imageBase64UploadFileTest() {
        uploadedImageId = given()
                .header("Authorization", token)
                .multiPart("image", encodedImage)
                .formParam("title", "ImageTitle")
                .formParam("name", "ImageName")
                .formParam("type", "base64")
                .formParam("description", "ImageDescription")
                .expect()
                .body("success", is(true))
                .body("data.id", is(notNullValue()))
                .body("data.type", is("image/jpeg"))
                .body("data.name", is("ImageName"))
                .body("data.description", is("ImageDescription"))
                .body("data.title", is("ImageTitle"))
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void imageWrongTypeUploadFileTest() {
        uploadedImageId = given()
                .header("Authorization", token)
                .multiPart("image", encodedImage)
                .formParam("title", "ImageTitle")
                .formParam("name", "ImageName")
                .formParam("type", "jpeg")
                .formParam("description", "ImageDescription")
                .expect()
                .statusCode(400)
                .body("success", is(false))
                .body("data.error", is("Bad Request"))
                .body("data.request", is("/3/upload"))
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek()
                .then()
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    void getImageTest() {
        given()
                .header("Authorization", token)
                .when()
                .get("https://api.imgur.com/3/image/{imageHash}", existingImageHash)
                .prettyPeek()
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", is("JoZqQmY"));
    }

    @Test
    void favoriteAnImageTest() {
        Response response = given()
                .header("Authorization", token)
                .when()
                .post("https://api.imgur.com/3/image/{imageHash}/favorite", existingImageHash)
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
                .header("Authorization", token)
                .when()
                .post("https://api.imgur.com/3/image/{imageHash}/favorite", "test")
                .prettyPeek();

        assertEquals(404, response.statusCode());
    }

    @Test
    void allEmptyUploadFileTest() {
         given()
                .header("Authorization", token)
                .formParam("title", "")
                .formParam("name", "")
                .formParam("type", "jpeg")
                .formParam("description", "")
                .expect()
                .statusCode(400)
                .body("success", is(false))
                .body("data.error", is("Bad Request"))
                .body("data.request", is("/3/upload"))
                .when()
                .post("https://api.imgur.com/3/upload")
                .prettyPeek();
    }



    @AfterEach
    void tearDown() {
        if (uploadedImageId != null) {
            given()
                    .header("Authorization", token)
                    .when()
                    .delete("https://api.imgur.com/3/image/{imageHash}", uploadedImageId)
                    .prettyPeek()
                    .then()
                    .statusCode(200);
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
