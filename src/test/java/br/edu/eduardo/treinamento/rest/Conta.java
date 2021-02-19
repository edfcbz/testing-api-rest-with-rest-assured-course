package br.edu.eduardo.treinamento.rest;

public class Conta {

	private Integer conta_id;
	private String conta;
	private Float saldo;

	public Conta() {
	}
	
	public Conta(Integer conta_id, String conta, Float saldo) {
		super();
		this.conta_id = conta_id;
		this.conta = conta;
		this.saldo = saldo;
	}
	
	public Integer getConta_id() {
		return conta_id;
	}
	public void setConta_id(Integer conta_id) {
		this.conta_id = conta_id;
	}
	public String getConta() {
		return conta;
	}
	public void setConta(String conta) {
		this.conta = conta;
	}
	public Float getSaldo() {
		return saldo;
	}
	public void setSaldo(Float saldo) {
		this.saldo = saldo;
	}
	
	
}


