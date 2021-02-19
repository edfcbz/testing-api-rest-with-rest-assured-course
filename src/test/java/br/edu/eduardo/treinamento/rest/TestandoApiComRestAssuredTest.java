package br.edu.eduardo.treinamento.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.matcher.RestAssuredMatchers.matchesXsdInClasspath;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.internal.path.xml.NodeImpl;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.path.xml.XmlPath.CompatibilityMode;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class TestandoApiComRestAssuredTest {
	
	private static RequestSpecification requestSpecification;
	private static ResponseSpecification responseSpecification;
	

	@BeforeClass
	public static void beforeClass() {
		RestAssured.baseURI = "https://restapi.wcaquino.me";
		RestAssured.port = 443;
		
		RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();				
		requestSpecification = requestSpecBuilder.build();
		requestSpecBuilder.log(LogDetail.ALL);
		
		ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder();
		responseSpecification = responseSpecBuilder.build();	
		responseSpecBuilder.log(LogDetail.ALL);

		RestAssured.requestSpecification  = requestSpecification;
		RestAssured.responseSpecification = responseSpecification;
	}
	
	
	@Test
	public void olaMundotest() {
		Response response = RestAssured.request(Method.GET, "/ola");

		ValidatableResponse validacao = response.then();
		

		assertTrue(response.getBody().asString().equals("Ola Mundo!"));
		assertTrue(response.statusCode() == 200);

		assertTrue("Status code incorreto", response.statusCode() == 200);
		assertEquals(200, response.statusCode());

		validacao.statusCode(200);
	}

	@Test
	public void conhecendoOutrasFormasdeRestAssured1() {
		get("/ola").then().statusCode(200);
	}

	@Test
	public void conhecendoOutrasFormasdeRestAssured2() {
		given()
		.when()
			.get("/ola")
		.then()
			.statusCode(200)
			.assertThat()
			.statusCode(200);
			
	}	
	
	@Test
	public void devoConhecerMatchersHamcrest() {
		assertThat("Maria", Matchers.is("Maria"));
		assertThat(128, Matchers.is(128));
		assertThat(128, Matchers.isA(Integer.class));
		assertThat(128D, Matchers.isA(Double.class));
		assertThat(128D, Matchers.greaterThan(120D));
		
		List<Integer> impares = Arrays.asList(1,3,5,7,9);
		
		assertThat(impares, hasSize(5));
		assertThat(impares, contains(1,3,5,7,9));
		assertThat(impares, containsInAnyOrder(3,1,5,7,9));
		assertThat(impares, hasItem(5));
		assertThat(impares, hasItems(5,9,3));
		assertThat("Maria", Matchers.is(not("João")));
		assertThat("Maria", Matchers.not("João"));
		assertThat("Maria", anyOf(is("Maria"), is("Carlos")) );
		assertThat("Carlos", anyOf(is("Maria"), is("Carlos")) );
		assertThat("Joaquina", allOf( startsWith("Jo"), endsWith("ina"), containsString("qui")  ) );
	}
	
	
	@Test
	public void devoValidarBody() {
		given()			
		.when()
			.get("/ola")
		.then()
			.statusCode(200)
			.body(is("Ola Mundo!"))
			.body(containsString("Mundo!"))
			.body(is(notNullValue()))
			;
	}
	
	
	@Test
	public void devoVerificarJsonPrimeiroNivel() {
		given()
		.when()
			.get("/users/1")
		.then()
			.statusCode(200)
			.body("id", is(1))
			.body("name", containsString("Silva"))
			.body("age", greaterThan(18))
			;
	}
	
	@Test
	public void devoVerificarJsonPrimeiroNivelOutrasFormas() {
		
		Response response = RestAssured.request(Method.GET, "/users/1");
	
		//Testes utilizando o Body contido dentro do response
		Assert.assertEquals(new Integer(1), response.path("id"));
		Assert.assertEquals(new Integer(1), response.path("%s", "id"));
		
		//Teste utilizando o Json que vem dentro do body do Response
		JsonPath jPath = new JsonPath(response.asString());
		
		assertEquals(1, jPath.getInt("id") );
		assertEquals("1", jPath.getString("id") );
		
		//Testes utilizando a propriedade estátia FROM do JsonPath
		int id = JsonPath.from(response.asInputStream()).getInt("id");
		assertEquals(1, id );
	}
	
	
	@Test
	public void devoVerificarJsonSegundoNivel() {
		given()
		.when()
			.get("/users/2")
		.then()
			.statusCode(200)
			.body("id", is(2))
			.body("endereco.rua", containsString("bobos"))
			;
	}
	
	@Test
	public void devoVerificarJsonComUmaLista() {
		given()
		.when()
			.get("/users/3")
		.then()
			.statusCode(200)
			.body("id", is(3))
			.body("name", containsString("Ana"))
			.body("filhos", hasSize(2)) 
			.body("filhos[0].name", is("Zezinho"))
			.body("filhos[1].name", is("Luizinho"))
			.body("filhos.name", hasItem("Zezinho"))
			.body("filhos.name", hasItems("Zezinho","Luizinho"))
			.body("filhos.name", containsInAnyOrder("Luizinho","Zezinho"))
			;
	}	
	
	@Test
	public void devoVerificarErroUsuarioInexistente() {
		given()
		.when()
			.get("/users/4")
		.then()
			.statusCode(404)
			.body("error", is("Usuário inexistente"))
			;
	}	
	
	@Test
	public void devoVerificarJsonComUmaListaNaRaiz() {
		given()
		.when()
			.get("/users")
		.then()
			.statusCode(200)	
			.body("$", hasSize(3))
			.body("", hasSize(3))
			.body("name", hasItems("João da Silva","Maria Joaquina","Ana Júlia"))
			.body("age[1]", is(25))
			.body("[2].filhos[0].name", is("Zezinho"))
			.body("filhos.name", hasItem( hasItem( "Luizinho")))
			.body("filhos.name", hasItem( containsInAnyOrder( "Luizinho", "Zezinho")))
			.body("filhos.name", hasItem( Arrays.asList( "Zezinho", "Luizinho")))
			.body("salary", contains( 1234.5678f, 2500, null ))
			;
	}	
	
	@Test
	public void devoVerificarOpcoesAvancadas() {
		
		given()
		.when()
			.get("/users")
		.then()
			.statusCode(200)	
			.body("$", hasSize(3))
			.body("age.findAll{it <= 25}.size()", is(2))
			.body("age.findAll{it <= 25 && it > 20 }.size()", is(1))
			.body("findAll{it.age <= 25 && it.age > 20 }.name", hasItem("Maria Joaquina"))
			.body("findAll{it.age <= 25 }[0].name", is("Maria Joaquina"))
			.body("findAll{it.age <= 25 }[-1].name", is("Ana Júlia"))
			.body("find{it.age <= 25 }.name", is("Maria Joaquina"))
			.body("findAll{it.name.contains('n')}.size()", greaterThan(1))
			.body("findAll{it.name.contains('n')}.name", hasItems("Maria Joaquina", "Ana Júlia"))
			.body("findAll{it.name.length() > 10}.name", hasItems("João da Silva", "Maria Joaquina"))
			.body("name.collect{ it.toUpperCase()}", hasItem("MARIA JOAQUINA"))
			.body("name.findAll{ it.startsWith('Maria')}.collect{it.toUpperCase()}", hasItem("MARIA JOAQUINA"))
			//.body("name.findAll{ it.startsWith('Maria')}.collect{it.toUpperCase()}.toArray()",allOf(arrayContaining("MARIA JOAQUINA"), arrayWithSize(1)))
			.body("name.findAll{ it.startsWith('Maria')}.collect{it.toUpperCase()}.toArray()",allOf(arrayContaining("MARIA JOAQUINA")))
			.body("age.collect{it * 2}", hasItems(60 , 50 , 40))
			.body("id.max()", is(3))
			.body("salary.min()", is(1234.5678f))
			.body("salary.findAll{ it != null}.sum()", is(3734.5677490234375))
			.body("salary.findAll{ it != null}.sum()", is( closeTo( 3734.5678f, 0.001)))
			.body("salary.findAll{ it != null}.sum()", allOf( greaterThan(3000d), lessThan(5000d) ))
			;
	}	
	
	@Test
	public void devoVerificarUtilizandoJsonPathComJAVA() {
		ArrayList<String> lista = 	given()
									.when()
										.get("/users")
									.then()
										.statusCode(200)	
										.extract().path("name.findAll{it.startsWith('Maria')}")
										;
		
		assertEquals( 1, lista.size() );
		assertTrue( lista.get(0).equalsIgnoreCase("maRia joAquiNa") );
		assertTrue( lista.get(0).equalsIgnoreCase("Maria JoaQUINA") );
		assertEquals( "maRia joAquiNa".toUpperCase(), lista.get(0).toUpperCase());
	}	
	
	
	@Test
	public void devoVerificarUtilizandoXML() {
		given()
		.when()
			.get("/usersXML/3")
		.then()
			.statusCode(200)	
			.body("user.name", is("Ana Julia"))
			.body("user.@id", is("3"))
			.body("user.filhos.name.size()", is(2))
			.body("user.filhos.name[0]", is("Zezinho"))
			.body("user.filhos.name[1]", is("Luizinho"))
			.body("user.filhos.name", hasItem("Luizinho"))
			.body("user.filhos.name", hasItems("Luizinho","Zezinho"))
		;
		
	}
	
	@Test
	public void devoVerificarUtilizandoRootXML() {
		given()
		.when()
			.get("/usersXML/3")
		.then()
			.statusCode(200)	
			.rootPath("user")
				.body("name", is("Ana Julia"))
				.body("@id", is("3"))
				
			.rootPath("user.filhos")
				.body("name.size()", is(2))
				.body("name[0]", is("Zezinho"))
				.body("name[1]", is("Luizinho"))
				.body("name", hasItem("Luizinho"))
				.body("name", hasItems("Luizinho","Zezinho"))
				
			.detachRootPath("filhos")
				.body("filhos.name.size()", is(2))
				
			.appendRootPath("filhos.name")
				.body("size()", is(2))
				.body("[0]", is("Zezinho"))
				.body("[1]", is("Luizinho"))
				.body("", hasItem("Luizinho"))
				.body("", hasItems("Luizinho","Zezinho"))		
		;
	}	
	
	@Test
	public void devoVerificarUtilizandoRecursoAvancadosXML() {
		given()
		.when()
			.get("/usersXML")
		.then()
			.statusCode(200)
			.body("users.user.size()", is(3))
			.body("users.user.findAll{ it.age.toInteger() <= 25 }.size()", is(2))
			.body("users.user.@id", hasItems("1", "2", "3"))
			.body("users.user.findAll{ it.age == 25 }.name", is("Maria Joaquina"))
			.body("users.user.findAll{ it.name.toString().contains('n')}.name", hasItems("Maria Joaquina","Ana Julia"))		
			.body("users.user.salary.find{ it != null }", is("1234.5678"))
			.body("users.user.salary.find{ it != null }.toDouble()", is(1234.5678d))
			.body("users.user.age.collect{ it.toInteger() * 2}", hasItems( 40,50,60 ))
			.body("users.user.name.findAll{ it.toString().startsWith('Maria')}.collect{it.toString().toUpperCase()}", is("MARIA JOAQUINA"))		
			
			.rootPath("users.user")			
				.body("size()", is(3))
				.body("findAll{ it.age.toInteger() <= 25 }.size()", is(2))
				.body("@id", hasItems("1", "2", "3"))
				.body("findAll{ it.age == 25 }.name", is("Maria Joaquina"))
				.body("findAll{ it.name.toString().contains('n')}.name", hasItems("Maria Joaquina","Ana Julia"))		
				.body("salary.find{ it != null }", is("1234.5678"))
				.body("salary.find{ it != null }.toDouble()", is(1234.5678d))
				.body("age.collect{ it.toInteger() * 2}", hasItems( 40,50,60 ))
				.body("name.findAll{ it.toString().startsWith('Maria')}.collect{it.toString().toUpperCase()}", is("MARIA JOAQUINA"))	
		;
	}	

	
	@Test
	public void devoVerificarUtilizandoRecursoAvancadosComXMLeJAVA() {
		ArrayList<NodeImpl> nomes =	given()		
			 						.when()
			 							.get("/usersXML")
			 						.then()
			 							.statusCode(200)
			 							.extract().path("users.user.name.findAll{ it.toString().contains('n')}")		
						;
		assertEquals(2, nomes.size());
		assertEquals("Maria Joaquina".toUpperCase(), nomes.get(0).toString().toUpperCase());
		assertTrue("ANA JULIA".equalsIgnoreCase(nomes.get(1).toString()));
		
	}	
	
	@Test
	public void devoVerificarUtilizandoRecursoAvancadosComXMLeXPath() {
		given()	
		.when()
			.get("/usersXML")
		.then()
		.statusCode(200)
		.body(hasXPath("count(/users/user)", is("3")))	
		.body(hasXPath("/users/user[@id = '1']"))
		.body(hasXPath("//user[@id = '1']"))
		.body(hasXPath("//name[text() = 'Luizinho']/../../name", is("Ana Julia")))
		.body(hasXPath("//name[text() = 'Ana Julia']/following-sibling::filhos", allOf(containsString("Zezinho"),containsString("Luizinho"))))
		.body(hasXPath("/users/user/name", is("João da Silva")))
		.body(hasXPath("//name", is("João da Silva")))
		.body(hasXPath("/users/user[2]/name", is("Maria Joaquina")))
		.body(hasXPath("/users/user[last()]/name", is("Ana Julia")))
		.body(hasXPath("count(/users/user/name[contains(.,'n')])", is("2")))
		.body(hasXPath("//user[age < 24]/name",is("Ana Julia")))
		.body(hasXPath("//user[age > 20 and age < 30]/name",is("Maria Joaquina")))
		.body(hasXPath("//user[age > 20][age < 30]/name",is("Maria Joaquina")))
		;
		
	}
	
	@Test
	public void devoValidarBodyUtilizandoLog() {		
		given()	
			.spec(requestSpecification)
		.when()
			.get("/ola")
		.then()
			.statusCode(200)
			.spec(responseSpecification)
			.body(is("Ola Mundo!"))
			.body(containsString("Mundo!"))
			.body(is(notNullValue()))
			;
	}	
	
	@Test
	public void devoSimplificarAplicacaoDosRequestAndResponseSpecification() {		
		given()	
		.when()
			.get("/ola")
		.then()
			.statusCode(200)
			.body(is("Ola Mundo!"))
			.body(containsString("Mundo!"))
			.body(is(notNullValue()))
			;
	}
	
	@Test
	public void deveSalvarUsuario() {
		given()
			.contentType("application/json")
			.body("{ \"name\":\"Dudu\",\"age\":45 }")
		.when()			
			.post("/users")
		.then()
			.statusCode(201)
			.log().all()			
			.body("id", is(notNullValue()))
			.body("name", is("Dudu"))
			.body("age", is(45))			
		;		
	}
	
	@Test
	public void deveSalvarUsuarioComplexo() {
		given()
			.contentType("application/json")
			.body("{ \"name\":\"Jose\",\"endereco\":{\"rua\":\"Rua dos complexos\",\"numero\":10},\"age\":45,\"salary\":1500 }")
		.when()			
			.post("/users")
		.then()
			.statusCode(201)
			.log().all()			
			.body("name", is("Jose"))
			.body("age", is(45))
		;
	
	}	
	
	@Test
	public void naoDeveSalvarUsuarioSemNome() {
		given()
			.log().all()
			.contentType("application/json")
			.body("{ \"age\":45 }")
		.when()			
			.post("/users")
		.then()
			.log().all()	
			.statusCode(400)
			.body("id", is( nullValue() ))
			.body("error", is("Name é um atributo obrigatório"))
		;
	
	}		
	
	
	@Test
	public void deveSalvarUsuarioXML() {
		given()
			.contentType(ContentType.XML)
			.body("<user><name>Joao Carlos</name><age>45</age></user>")
		.when()			
			.post("/usersXML")
		.then()
			.statusCode(201)
			.log().all()			
			.body("user.@id", is(notNullValue()))
			.body("user.name", is("Joao Carlos"))
			.body("user.age", is("45"))			
		;		
	}
	
	@Test
	public void deveALterarUsuarioComPUT() {
		given()
			.contentType("application/json")
			.body("{ \"id\":\"1\",\"name\":\"Dudu Carneiro\",\"age\":\"18\" }")
		.when()			
			.put("/users/1")
		.then()
			.statusCode(200)
			.log().all()			
			.body("id", is("1"))
			.body("name", is("Dudu Carneiro"))
			.body("age", is("18"))			
			.body("salary", is(1234.5678f))
		;
	
	}	
	
	@Test
	public void deveUtilizarUrlComParametrizado() {
		given()
			.contentType("application/json")
			.body("{ \"id\":\"1\",\"name\":\"Dudu Carneiro\",\"age\":\"18\" }")
		.when()			
			.put("/{entidade}/{userId}","users","1")
		.then()
			.statusCode(200)
			.log().all()			
			.body("id", is("1"))
			.body("name", is("Dudu Carneiro"))
			.body("age", is("18"))			
			.body("salary", is(1234.5678f))
		;	
	}	
	
	@Test
	public void deveUtilizarUrlComParametrizadoParte2() {
		given()
			.contentType("application/json")
			.body("{ \"id\":\"1\",\"name\":\"Dudu Carneiro\",\"age\":\"18\" }")
			.pathParam("entidade", "users")
			.pathParam("userId", "1")
		.when()			
			.put("/{entidade}/{userId}")
		.then()
			.statusCode(200)
			.log().all()			
			.body("id", is("1"))
			.body("name", is("Dudu Carneiro"))
			.body("age", is("18"))			
			.body("salary", is(1234.5678f))
		;	
	}		
	
	@Test
	public void deveRemoverUsuarioUtilizandoVerboDelete() {
		given()
			.log().all()
		.when()			
			.delete("/users/1")
		.then()			
			.log().all()	
			.statusCode(204)
		;	
	}	
	
	@Test
	public void NaoDeveRemoverUsuarioUtilizandoInexistente() {
		given()
			.log().all()
		.when()			
			.delete("/users/4")
		.then()			
			.log().all()	
			.statusCode(400)
			.body("error", is("Registro inexistente"))
		;	
	}	

	@Test
	public void deveSalvarUsuarioRecebendoUmMapComoEntradaPoisNesseExemploEhPassadoUmaSrtring() {
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", "Usuario via map");
		params.put("age", 25);
		
		given()
			.log().all()
			.contentType("application/json")
			.body(params)
		.when()			
			.post("/users")
		.then()
			.log().all()
			.statusCode(201)
			.body("id", is(notNullValue()))
			.body("name", is("Usuario via map"))
			.body("age", is(25))			
		;		
	}
	
	@Test
	public void deveSalvarUsuarioRecebendoUmObjetoComoEntrada() {
		
		User usuario = new User("Usuario via objeto", 20);
		
		given()
			.log().all()
			.contentType("application/json")
			.body(usuario)
		.when()			
			.post("/users")
		.then()
			.log().all()
			.statusCode(201)
			.body("id", is(notNullValue()))
			.body("name", is("Usuario via objeto"))
			.body("age", is(20))			
		;		
	}
	
	@Test
	public void deveDeserializarObjetoAoSalvarUsuario() {
		
		User usuario = new User("Usuario deserializado", 20);
		
		User retorno =given()
						.log().all()
						.contentType("application/json")
						.body(usuario)
					.when()			
						.post("/users")
					.then()
						.log().all()
						.statusCode(201)
						.body("id", is(notNullValue()))
						.extract().body().as(User.class)			
		;	
		
		assertEquals(usuario.getName(), retorno.getName());
		assertEquals(usuario.getAge(), retorno.getAge());
		
		assertThat(retorno.getName(), is(usuario.getName()));
		assertThat(retorno.getId(), notNullValue());
		
	}	
	
	
	@Test
	public void deveSalvarUsuarioViaXMLUsandoObjeto() {
		
		User usuario = new User("Usuario serializado", 30);

		given()
			.contentType(ContentType.XML)
			.body(usuario)
		.when()			
			.post("/usersXML")
		.then()
			.statusCode(201)
			.log().all()			
			.body("user.@id", is(notNullValue()))
			.body("user.name", is("Usuario serializado"))
			.body("user.age", is("30"))			
		;		
	}
	
	
	@Test
	public void deveDeserializarUsuarioViaXMLUsandoObjeto() {
		
		User usuario = new User("Usuario serializado from xml", 30);

		User retorno = given()
			.contentType(ContentType.XML)
			.body(usuario)
		.when()			
			.post("/usersXML")
		.then()
			.statusCode(201)
			.log().all()			
			.extract().body().as(User.class)			
		;	
		
		assertEquals(usuario.getName(), retorno.getName());
		assertEquals(usuario.getAge(), retorno.getAge());
		
		assertThat(retorno.getName(), is(usuario.getName()));
		assertThat(retorno.getId(), notNullValue());
	}	
	
	
	@Test
	public void deveEnviarValorViaQUERY_XML() {
		given()
			.log().all()
		.when()			
			.get("/v2/users?format=xml")
		.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.XML)	
			
		;	
		
	}	
	
	
	@Test
	public void deveEnviarValorViaQUERY_JSON() {
		given()
			.log().all()	
		.when()			
			.get("/v2/users?format=json")
		.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)	
			
		;	
		
	}
	
	
	@Test
	public void deveEnviarValorViaPARAMS_JSON() {
		given()
			.log().all()
			.queryParam("format", "json")
		.when()			
			.get("/v2/users")
		.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)	
			
		;	
		
	}	
	
	@Test
	public void deveEnviarValorViaPARAMS_XML() {
		given()
			.log().all()
			.queryParam("format", "xml")
		.when()			
			.get("/v2/users")
		.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.XML)	
			
		;	
		
	}		
	
	@Test
	public void deveEnviarValorViaPARAMS_SEMEFEITO() {
		given()
			.log().all()
			.queryParam("format", "xml")
			.queryParam("nome", "meu nome")
			.queryParam("endereco", "meu endereco")
			.queryParam("idade", "20")
		.when()			
			.get("/v2/users")
		.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.XML)	
			.contentType(containsString("utf-8"))
			
		;	
		
	}	
	
	@Test
	public void deveEnviarValorViaHEADER() {
		given()
			.log().all()
			.accept(ContentType.JSON)
			.queryParam("nome", "meu nome")
			.queryParam("endereco", "meu endereco")
			.queryParam("idade", "20")
		.when()			
			.get("/v2/users")
		.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)			
		;	
		
	}
	
	
	@Test
	public void deveFazerBuscasComHTML() {
		given()
			.log().all()
		.when()			
			.get("/v2/users")
		.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.HTML)		
			.body("html.body.div.table.tbody.tr.size()", is(3))
			.body("html.body.div.table.tbody.tr[1].td[2]", is("25"))
			.appendRootPath("\"html.body.div.table.tbody")
			.body("tr.size()", is(3))
			.body("tr[1].td[2]", is("25"))
			
		;			
	}	
	
	
	@Test
	public void deveFazerBuscasComHTMLIterandoNasLinhas() {
		given()
			.log().all()
		.when()			
			.get("/v2/users")
		.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.HTML)		
			.body("html.body.div.table.tbody.tr.size()", is(3))
			.body("html.body.div.table.tbody.tr[1].td[2]", is("25"))
			.appendRootPath("\"html.body.div.table.tbody")
			.body("tr.size()", is(3))
			.body("tr.find{it.toString().startsWith('2')}.td[1]", is("Maria Joaquina"))
			
		;			
	}	
	
	@Test
	public void deveFazerBuscasComXPATHemHTML() {
		given()
			.log().all()
		.when()			
			.get("/v2/users/?format=clean")
		.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.HTML)		
			.body(hasXPath("count(//table/tr)", is("4")))
			.body(hasXPath("//td[text()='2']/../td[2]", is("Maria Joaquina")))
		;			
	}	
	
	
	@Test
	public void deveObrigarEnvioArquivo() {
		given()
			.log().all()
		.when()			
			.post("/upload")
		.then()
			.log().all()
			.body("error", is("Arquivo não enviado") )
		;			
	}
	
	
	@Test
	public void deveEnviarArquivo() {
		given()
			.log().all()
			.multiPart("arquivo", new File("src/test/resources/users.pdf"))
		.when()
			.post("/upload")
		.then()
			.log().all()
			.statusCode(200)
			.body("name", is("users.pdf"))
		;			
	}	
	
	@Test
	public void deveEnviarArquivoMuitoGrande() {
		given()
			.log().all()
			.multiPart("arquivo", new File("src/test/resources/IMM5802E.pdf"))
		.when()
			.post("/upload")
		.then()
			.log().all()
			.time(lessThan(10000L))
			.statusCode(413)
		;			
	}
	
	@Test
	public void deveFazerDownloadDeArquivo() throws IOException {
		byte[] image = given()
			.log().all()
		.when()
			.get("/download")
		.then()
			.log().all()
			.statusCode(200)
			.extract().asByteArray()
		;	
		
		File imagem = new  File("src/test/resources/imagem.jpg");
		OutputStream outPut = new  FileOutputStream(imagem);
		outPut.write(image);
		outPut.close();
		
		assertThat(imagem.length(), is(94878L));
		
	}	
	
	
	@Test
	public void deveValidarSquemaXML() throws IOException {
		given()
			.log().all()
		.when()
			.get("/usersXML")
		.then()
			.log().all()
			.statusCode(200)
			.body(matchesXsdInClasspath("users.xsd"))
		;	
	}
	
	
	@Test
	public void deveValidarSquemaJSON() throws IOException {
		given()
			.log().all()
		.when()
			.get("/users")
		.then()
			.log().all()
			.statusCode(200)
			.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("users.json"))
		;	
	}	
	
	@Test(expected=SAXParseException.class)
	public void naoDeveValidarSquemaXMLInvalido() throws IOException {
		given()
			.log().all()
		.when()
			.get("/invalidUsersXML")
		.then()
			.log().all()
			.statusCode(200)
			.body(matchesXsdInClasspath("users.xsd"))
		;	
	}	
	
	@SuppressWarnings("static-access")
	@Test
	public void deveAcessarAplicacaoWEB() throws IOException {
		
		
		String header = new RestAssured().given()
			 				.log().all()
			 				.formParam("email", "wagner@aquino")
			 				.formParam("senha", "123456")
			 				.contentType(ContentType.URLENC.withCharset("UTF-8"))
						.when()
							.post("http://seubarriga.wcaquino.me:80/logar")
						.then()
							.log().all()
							.statusCode(200)
							.extract().header("set-cookie")							
						;	
				 
				 
				 header = header.split("=")[1].split(";")[0];
				 System.out.println("Cookie:"+header);
				 
				 
				 String path = new RestAssured().given()
									 				.log().all()
									 				.cookie("connect.sid",header)
												.when()
													.get("http://seubarriga.wcaquino.me:80/contas")
												.then()
													.log().all()
													.statusCode(200)
													.body("html.body.table.tbody.tr[1].td[0]", is("Conta com movimentacao"))
													.extract().body().asString()
												;	

				XmlPath xmlPath = new XmlPath(CompatibilityMode.HTML, path);
				String valorExtraido = xmlPath.getString("html.body.table.tbody.tr[1].td[0]");
				assertEquals("Conta com movimentacao", valorExtraido);
				 
	}	

	
}






















