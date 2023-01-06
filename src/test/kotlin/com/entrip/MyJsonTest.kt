package com.entrip

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext

@MockBean(JpaMetamodelMappingContext::class)
@JsonTest
class MyJsonTest(@Autowired val json: JacksonTester<VehicleDetails>) {

    class VehicleDetails(val make: String, val model: String) {}

    @Test
    fun serialize() {
        val details = VehicleDetails("Honda", "Civic")
        val expectedJson = "{\"make\":\"Honda\",\"model\":\"Civic\"}"
        assertThat(json.write(details)).isEqualTo(expectedJson)

        // Assert against a `.json` file in the same package.json as the test
        // assertThat(json.write(details)).isEqualToJson("package.json")

        // Or use JSON path based assertions
        assertThat(json.write(details)).hasJsonPathStringValue("make")
        assertThat(json.write(details)).extractingJsonPathStringValue("make").isEqualTo("Honda")
        //assertThat(json.write(details)).extractingJsonPathArrayValue<>()
        //assertThat(json.write(details)).extractingJsonPathBooleanValue()
        //assertThat(json.write(details)).extractingJsonPathNumberValue()
        //assertThat(json.write(details)).extractingJsonPathMapValue<>()
    }

    @Test
    fun deserialize() {
        val content = "{\"make\":\"Ford\",\"model\":\"Focus\"}"
        assertThat(json.parse(content)).isEqualTo(VehicleDetails("Ford", "Focus"))
        assertThat(json.parseObject(content).make).isEqualTo("Ford")
    }

}