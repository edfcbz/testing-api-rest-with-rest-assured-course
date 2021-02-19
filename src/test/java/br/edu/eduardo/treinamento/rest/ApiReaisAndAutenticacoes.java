package br.edu.eduardo.treinamento.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.Detail;

import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class ApiReaisAndAutenticacoes {

	
	@Test
	public void deveAcessarSwapi() {
	
	given()
		.log().all()
	.when()
		.get("https://swapi.dev/api/people/1/")
	.then()
		.log().all()
		.statusCode(200)
		.body("name", is("Luke Skywalker"));
	}
	
	@Test
	public void deveAcessarUtilizandoChaveAutenticacaoNaURL() {
	// 85ea20d1ae6584879b6017d4bd78fbda
		//AUTENTICANDO POR CHEVE QUE VAI COMO PARAMETRO NA URL

	given()
		.log().all()
		.queryParam("q", "Fortaleza,BR")
		.queryParam("appid","85ea20d1ae6584879b6017d4bd78fbda")
		.queryParam("units" , "metric" )
	.when()
		.get("http://api.openweathermap.org/data/2.5/weather")
	.then()
		.log().all()
		.statusCode(200)
		.body("name", is("Fortaleza"))
		.body("coord.lon", is(-38.52f))
		.body("coord.lat", is(-3.72f))
		;
	}	

	
	@Test
	public void NaoDeveAcessarSemInformarSenhaAndLoginDoUsuario() {
	// 85ea20d1ae6584879b6017d4bd78fbda
		//AUTENTICANDO POR CHEVE QUE VAI COMO PARAMETRO NA URL

	given()
		.log().all()
	.when()
		.get("http://restapi.wcaquino.me/basicauth")
	.then()
		.log().all()
		.statusCode(401)
		;
	}	
	
	@Test
	public void DeveAcessarComSenhaAndLoginDoUsuario() {

	given()
		.log().all()
	.when()
		.get("http://admin:senha@restapi.wcaquino.me/basicauth")
	.then()
		.log().all()
		.statusCode(200)
		.body("status", is("logado"))
		;
	
	}
	
	@Test
	public void DeveAcessarComSenhaAndLoginDoUsuario2() {

	given()
		.log().all()
		.auth().basic("admin", "senha")
	.when()
		.get("http://restapi.wcaquino.me/basicauth")
	.then()
		.log().all()
		.statusCode(200)
		.body("status", is("logado"))
		;
	
	}	

	@Test
	public void DeveAcessarComSenhaAndLoginDoUsuarioChallenger() {

	given()
		.log().all()
		.auth().preemptive().basic("admin", "senha")
	.when()
		.get("http://restapi.wcaquino.me/basicauth2")
	.then()
		.log().all()
		.statusCode(200)
		.body("status", is("logado"))
		;
	
	}	
	
	
	@Test
	public void DeveAcessarComTokenAWT() {
		//Deve acessar a API utilizando o TOKEN do usuário
		Map<String, String> login = new HashMap<>();

		login.put("email", "edfcbz@gmail.com");
		login.put("senha", "123456");
		
		String token = given()
			.log().all()
			.body(login)
			.contentType(ContentType.JSON)
		.when()
			.post("http://barrigarest.wcaquino.me/signin")
		.then()
			.log().all()
			.statusCode(200)
			.extract().path("token")
			;
	
		given()
			.log().all()
			.header("Authorization", "JWT "+ token)
		.when()
			.get("http://barrigarest.wcaquino.me/contas")
		.then()
			.log().all()
			.statusCode(200)
			.body("usuario_id", hasItem(10409))
			;		
		
		
	}	
	
}
