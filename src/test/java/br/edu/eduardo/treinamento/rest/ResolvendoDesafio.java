package br.edu.eduardo.treinamento.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class ResolvendoDesafio extends BaseTest {

	//private static String token;
	
	private static String CONTA_NAME;
	
	@Before
	public void before() {
		CONTA_NAME = "Conta "+ System.nanoTime();
	}
	
	@BeforeClass
	public static void login() {		
		Map<String, String> login = new HashMap<String, String>();
		login.put("email", "edfcbz@gmail.com");
		login.put("senha", "123456");
		
		String token = given()
 					.body(login)			
 				.when()
 					.post("/signIn")
 				.then() 		
 					.statusCode(200)
 					.extract().path("token")
				;
		
		RestAssured.requestSpecification.header("Authorization", "JWT "+token );		
		RestAssured.get("/reset").then().statusCode(200);		
	}
	
	
	@Test
	public void naoDeveAcessarApiSemTokem() {
				//Acessar GET/Contas
		
		//Estrutura para retirar o Autenticador (TOKEN) passado para todos os testes durante o @Before
		
		//FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		//req.removeHeader("Authorization");
		
		//ESTA MARCANDO ERRADO POIS IMPLICITAMENTE O TOKEM EST´PA SENDO PASSADO NA ESTRUTURA @BEFORE.
		given()
			.when()
		.post("/contas")
			.then()
		.statusCode(400) 										
;				
	}
	
	@Test
	public void deveIncluirContaComSucesso() {
		//Acessar POST/signIn
		given()
			.body("{\"nome\": \""+ CONTA_NAME +"\"}")	
		.when()
			.post("/contas")
		.then()
			.statusCode(201)			
		;	
	}
	
	@Test
	public void deveAlterarContaComSucesso() {
		
		Integer id = given()
			.body("{\"nome\": \""+CONTA_NAME+"\"}")	
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.extract().path("id")
		;
		
		given()
			.body("{\"nome\": \"Alterada "+CONTA_NAME+"\"}")	
		.when()
			.put("/contas/"+id)
		.then()
			.statusCode(200)			
		;	
		
		//Solução alternativa de enviar o parametro para alteração
		given()
			.body("{\"nome\": \"Alterada "+CONTA_NAME+"\"}")	
			.pathParam("id", id)
		.when()
			.put("/contas/{id}")
		.then()
			.statusCode(200)			
	;		
		
	}	
	
	@Test
	public void naoDeveIncluirContaComNomeRepetido() {
		
		String parteDinamica = UUID.randomUUID().toString();
		
		
		given()
			.body("{\"nome\": \"Conta Duplicada" +parteDinamica+ "\"}")	
		.when()
			.post("/contas")
			.then()
		.statusCode(201)
	;
		
		given()
			.body("{\"nome\": \"Conta Duplicada"+parteDinamica+"\"}")	
		.when()
			.post("/contas")
			.then()
		.statusCode(400)
		.body("error", is("Já existe uma conta com esse nome!"))
;	
		
	}	
	
	@Test
	public void InserirMovimentacaoComSucesso() {
		String retorno = given()
							.body("{\"nome\": \"" + CONTA_NAME  +  "\"}")	
						.when()
							.post("/contas")
						.then()
							.statusCode(201)
							.extract().asString()		
	;	
		
		String idConta = retorno.split(":")[1].split(",")[0];

		Movimentacao movimentacao = new Movimentacao();
		movimentacao.setConta_id(Integer.parseInt(idConta));
		movimentacao.setDescricao("SALARIO");
		movimentacao.setEnvolvido("Meu nome");
		movimentacao.setTipo("REC");
		movimentacao.setData_transacao( DateUtils.obterDataFormatada(new Date()) );
		movimentacao.setData_pagamento( DateUtils.obterDataDiferencaDias(30) );
		movimentacao.setValor(2000F);
		movimentacao.setStatus(true);
		
		given()
			.body(movimentacao)	
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201);
		
		movimentacao.setConta_id(Integer.parseInt(idConta));
		movimentacao.setDescricao("Conta de luz");
		movimentacao.setEnvolvido("Coelce");
		movimentacao.setTipo("DESP");
		movimentacao.setData_transacao( DateUtils.obterDataFormatada(new Date()) );
		movimentacao.setData_pagamento( DateUtils.obterDataDiferencaDias(30) );
		movimentacao.setValor(150f);
		movimentacao.setStatus(true);
		
		given()
			.body(movimentacao)	
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201);
		
		movimentacao.setConta_id(Integer.parseInt(idConta));
		movimentacao.setDescricao("Conta de Água");
		movimentacao.setEnvolvido("CAGECE");
		movimentacao.setTipo("DESP");
		movimentacao.setData_transacao( DateUtils.obterDataFormatada(new Date()) );
		movimentacao.setData_pagamento( DateUtils.obterDataDiferencaDias(30) );
		movimentacao.setValor(75f);
		movimentacao.setStatus(true);
		
		given()
			.body(movimentacao)	
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201);
		
	}		
	
	@Test
	public void deveValidarCamposObrogatoriosNaTransacao() {
			
			Movimentacao movimentacao = new Movimentacao();
			ArrayList<String> mensagens = given()
											.body(movimentacao)	
										.when()
											.post("/transacoes")
										.then()
											.extract().path("msg")
			;
			
			assertEquals("Data da Movimentação é obrigatório", mensagens.get(0).toString());
			assertEquals("Data do pagamento é obrigatório", mensagens.get(1).toString());
			assertEquals("Descrição é obrigatório", mensagens.get(2).toString());
			assertEquals("Interessado é obrigatório", mensagens.get(3).toString());
			assertEquals("Valor é obrigatório", mensagens.get(4).toString());
			assertEquals("Valor deve ser um número", mensagens.get(5).toString());
			assertEquals("Conta é obrigatório", mensagens.get(6).toString());
			assertEquals("Situação é obrigatório", mensagens.get(7).toString());
			
			//Solução Wagner
			given()
				.body("{}")	
			.when()
				.post("/transacoes")
				.then()
			.body("$", hasSize(8))
			.body("msg", containsInAnyOrder("Data da Movimentação é obrigatório",
											"Data do pagamento é obrigatório",
											"Descrição é obrigatório",
											"Interessado é obrigatório",
											"Valor é obrigatório",
											"Valor deve ser um número",
											"Conta é obrigatório",
											"Situação é obrigatório"));
	}
	
	@Test
	public void naoDeveCadastrarMovimentacaoFutura() {
		String retorno = given()
				.body("{\"nome\": \"" + CONTA_NAME  +  "\"}")	
			.when()
				.post("/contas")
			.then()
				.statusCode(201)
				.extract().asString()		
;	

			String idConta = retorno.split(":")[1].split(",")[0];
			
			Movimentacao movimentacao = new Movimentacao();
			movimentacao.setConta_id(Integer.parseInt(idConta));
			movimentacao.setDescricao("SALARIO");
			movimentacao.setEnvolvido("Meu nome");
			movimentacao.setTipo("REC");
			
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.getTime();
			
			movimentacao.setData_transacao( DateUtils.obterDataDiferencaDias(2));
			movimentacao.setData_pagamento( DateUtils.obterDataFormatada(new Date()));
			movimentacao.setValor(2000F);
			movimentacao.setStatus(true);
			
			ArrayList<String> mensagens = given()
				.body(movimentacao)	
			.when()
				.post("/transacoes")
			.then()
			    .extract().path("msg");
						
			assertEquals("Data da Movimentação deve ser menor ou igual à data atual", mensagens.get(0).toString());
			
			//Solução Wagner
			given()
				.body(movimentacao)	
			.when()
				.post("/transacoes")
			.then()
		    	.statusCode(400)
		    	.body("msg[0]", is("Data da Movimentação deve ser menor ou igual à data atual"))
		    	.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
		    	.body("$", hasSize(1))
		    	;
	}	
	
	@Test
	public void naoDeveRemoverContaComMovimentacao() {
		//Acessar DELETE/contas/:id
		
		String retorno = given()
				.body("{\"nome\": \"" + CONTA_NAME +  "\"}")	
			.when()
				.post("/contas")
			.then()
				.statusCode(201)
				.extract().asString()		
;	

			String idConta = retorno.split(":")[1].split(",")[0];
			
			Movimentacao movimentacao = new Movimentacao();
			movimentacao.setConta_id(Integer.parseInt(idConta));
			movimentacao.setDescricao("SALARIO");
			movimentacao.setEnvolvido("Meu nome");
			movimentacao.setTipo("REC");
			movimentacao.setData_transacao( DateUtils.obterDataFormatada(new Date()) );
			movimentacao.setData_pagamento( DateUtils.obterDataDiferencaDias(30) );
			movimentacao.setValor(2000F);
			movimentacao.setStatus(true);
			
			given()
				.body(movimentacao)	
			.when()
				.post("/transacoes")
			.then()
				.statusCode(201);		
		
			given()
			.when()
				.delete("/contas/"+idConta)
			.then()
				.statusCode(500)	
				.body("constraint",is("transacoes_conta_id_foreign"))//wagner
				;
	
		
	}		
	
	
	@Test
	public void deveCalcularSaldoDasContas() {
		//Acessar GET/saldo (Somenteretorna contas com movimentação)

			given()
			.when()
				.get("/saldo")
			.then()
				.extract().path("saldo");
		
			given()
			.when()
				.get("/saldo")
			.then()
				.statusCode(200)
				.body("saldo.size()", greaterThan(1));			
			
		//assertTrue(!somatorio.equals(null));
		
	}
	
	@Test
	public void deveRemoverMovimentacao() {
		//Acessar DELETE/transacoes/:id
		String retorno = given()
				.body("{\"nome\": \"" + CONTA_NAME +  "\"}")	
			.when()
				.post("/contas")
			.then()
				.statusCode(201)
				.extract().asString()		
;	

			String idConta = retorno.split(":")[1].split(",")[0];
			
			Movimentacao movimentacao = new Movimentacao();
			movimentacao.setConta_id(Integer.parseInt(idConta));
			movimentacao.setDescricao("SALARIO");
			movimentacao.setEnvolvido("Meu nome");
			movimentacao.setTipo("REC");
			movimentacao.setData_transacao( DateUtils.obterDataFormatada(new Date()) );
			movimentacao.setData_pagamento( DateUtils.obterDataDiferencaDias(30) );
			movimentacao.setValor(2000F);
			movimentacao.setStatus(true);
			
			Integer id = given()
				.body(movimentacao)	
			.when()
				.post("/transacoes")
				.then()
			.extract().path("id");

			given()
				.body(movimentacao)	
			.when()
				.delete("/transacoes/"+id)
			.then()
				.statusCode(204);
			
			
	}
	
	
}
