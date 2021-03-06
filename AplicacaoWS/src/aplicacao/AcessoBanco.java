package aplicacao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AcessoBanco {
	private String host = "http://localhost";
	private String porta = "5984";
	private String nomeBanco = "agenda";
	private String localBusca = "_design/listaContatos/_view/todos";
	private String pegaDocumento = "value";
	private URL urlConsulta;
	private String capturaJson;

	public AcessoBanco() {
		if (!verificaBancoCriado())
			inicializaBanco();
	}

	/**
	 * Esta função fará a busca por todos os documentos do banco Agenda, de
	 * acordo com a condição recebida como parâmetro, e retornará uma lista com
	 * os registros retornados pela chamada da função getRegistro() e manipulada
	 * como JSON.
	 * 
	 * @param condicao
	 * @return
	 */
	public List<Contato> buscaDocumentos(String condicao) {
		List<Contato> listaContatos = new ArrayList<Contato>();
		JSONObject json, documento, dadosContato;
		JSONArray arrayDocumentos;
		String url = getHost() + ":" + getPorta() + "/" + getNomebanco() + "/"
				+ getLocalBusca();

		/*
		 * Verifica a url usada para busca: - se url ==
		 * "_all_docs?include_docs=true", a condicao iniciará com & - se url ==
		 * "_design/listaContato/_view/todos", a condicao iniciará com ?
		 */

		if (getLocalBusca().contains("_all") && !condicao.equals("todos")) {
			String condicaoModificada = "&"
					+ condicao.substring(1, condicao.length());
			condicao = condicaoModificada;
		}

		if (!condicao.equals("todos"))
			url += condicao;

		String jsonString = getRegistro(url);
		/*
		 * Se a busca falhou com uma visão que pode não estar criada, a url de
		 * busca irá mudar para a padrão. A variável pegaDocumento altera de
		 * acordo com a url de busca, pois é ela que contem todos os dados de um
		 * registro de Contato. Caso a url seja usando a visão criada, essa
		 * variável recebe o valor "value". Caso contrário, em que a url seja a
		 * padrão, essa variável recebe o valor "doc".
		 */
		if (jsonString == null) {
			setLocalBusca("_all_docs?include_docs=true");
			pegaDocumento = "doc";
			url = getHost() + ":" + getPorta() + "/" + getNomebanco() + "/"
					+ getLocalBusca();
			if (!condicao.equals("todos"))
				url += condicao;
			jsonString = getRegistro(url);
		}

		try {
			json = new JSONObject(jsonString.toString());
			arrayDocumentos = json.getJSONArray("rows");
			for (int i = 0; i < arrayDocumentos.length(); i++) {
				try {
					Contato contato = new Contato();
					documento = arrayDocumentos.getJSONObject(i);
					dadosContato = documento.getJSONObject(pegaDocumento);
					contato.setId(dadosContato.getString("_id"));
					contato.setNome(dadosContato.getString("nome"));
					contato.setApelido(dadosContato.getString("apelido"));
					contato.setDataNascimento(dadosContato
							.getString("datanascimento"));
					contato.setTelefoneResidencial(dadosContato
							.getString("telres"));
					contato.setTelefoneCelular(dadosContato.getString("telcel"));
					contato.setCidade(dadosContato.getString("cidade"));
					contato.setEstado(dadosContato.getString("estado"));
					listaContatos.add(contato);
				} catch (NumberFormatException e) {
				}
			}
			return listaContatos;
		} catch (JSONException e1) {
			System.out
					.println("Erro ao manipular JSON do registro! - buscaDocumentos()/AcessoBanco.java");
		} catch (NullPointerException e2) {
			System.out
					.println("Erro na busca por documentos - formatação da URL - buscaDocumentos()/AcessoBanco.java");
		}
		return listaContatos;
	}

	/**
	 * Esta função chama o método getRegistro() para a verificação da existência
	 * do banco Agenda. Caso exista, a string retornada conterá informações do
	 * banco. Caso contrário, a string possuirá valor null e o valor de retorno
	 * desta função será false.
	 * 
	 * @return
	 */
	public boolean verificaBancoCriado() {
		if (!verificaInstalacaoCouchDB()) {
			JOptionPane
					.showMessageDialog(null,
							"O CouchDB não está instalado em seu sistema",
							"Erro na execução do aplicativo",
							JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		setNomeBanco("agenda");
		String url = getHost() + ":" + getPorta() + "/" + getNomebanco();
		String jsonRetornado = getRegistro(url);
		if (jsonRetornado == null)
			return false;
		return true;
	}

	/**
	 * Função que verifica se o couchDB está instalado no sistema
	 */
	public boolean verificaInstalacaoCouchDB() {
		String url = getHost() + ":" + getPorta();
		String jsonRetornado = getRegistro(url);
		if (jsonRetornado == null)
			return false;
		return true;
	}

	/**
	 * Esta função será chamada caso o banco Agenda não esteja criado até a
	 * momento de execução do aplicativo. A função chamará a função setRegistro
	 * que realizará a requisição ao CouchDB.
	 * 
	 */

	public void inicializaBanco() {
		setRegistro("");
	}

	/**
	 * O couchDB possui um mecanismo que gera um novo id sempre que for
	 * necessário incluir um novo registro. Esta função chama o método
	 * getRegistro() para extrair esta informação do JSON obtido pelo mecanismo
	 * de geração _uuids do couchDB.
	 * 
	 * @param idDocumento
	 * @return
	 */

	public String buscaId() {
		String url = getHost() + ":" + getPorta() + "/" + "_uuids";

		// fazendo a requisicao de um JSON com o novo id
		String jsonRetornado = getRegistro(url);
		JSONObject json;
		try {
			json = new JSONObject(jsonRetornado.toString());
			String uuid = json.getString("uuids");
			String idContato = uuid.substring(2, uuid.length() - 2);
			return idContato;
		} catch (JSONException e) {
			System.out
					.println("Erro ao manipular JSON! - buscaRevisaoContato()/AcessoBanco.java");
			return null;
		}
	}

	/**
	 * O couchDB precisa, para exclusão e alteração de algum registro, do hash
	 * de revisão do registro atual. Esta função chama o método getRegistro()
	 * para extrair esta informação do JSON obtido.
	 * 
	 * @param idDocumento
	 * @return
	 */

	public String buscaRevisaoContato(String idDocumento) throws IOException {
		String url = getHost() + ":" + getPorta() + "/" + getNomebanco() + "/"
				+ idDocumento;

		// fazendo a requisicao do hash do documento do aluno
		String jsonRetornado = getRegistro(url);
		JSONObject json;
		try {
			json = new JSONObject(jsonRetornado.toString());
			String revisao = json.getString("_rev");
			return revisao;
		} catch (JSONException e) {
			System.out
					.println("Erro ao manipular JSON! - buscaRevisaoContato()/AcessoBanco.java");
			return null;
		}
	}

	/**
	 * Método que recebe o id do documento como parâmetro e monta a string que
	 * será utilizada como URL para exclusão do registro.
	 * 
	 * @param idDocumento
	 */

	public void deletarContato(String idDocumento) {
		// busca o hash do aluno que será deletado
		String hash;
		try {
			hash = buscaRevisaoContato(idDocumento);
			// montando a url para deletar o documento do aluno
			String url = getHost() + ":" + getPorta() + "/" + getNomebanco()
					+ "/" + idDocumento + "?rev=" + hash;
			removeRegistro(url);
		} catch (IOException e) {
			System.out
					.println("Erro IOException - deletarAluno()/AcessoBanco.java");
		}
	}

	/**
	 * Função que monta o json de atualização do contato. Os parâmetros
	 * recebidos são o número do documento, que será o seu id no banco, e o json
	 * montado com os dados fornecidos para a atualização.
	 * 
	 * @param idDocumento
	 * @param json
	 */
	public void atualizarContato(String idDocumento, String dados) {
		try {
			String revisao = buscaRevisaoContato(idDocumento);
			// montando a url para deletar o documento do aluno
			String url = getHost() + ":" + getPorta() + "/" + getNomebanco()
					+ "/";
			/*
			 * acrescenta o id e o hash do aluno à string json, sem isso o
			 * documento não é alterado corretamente
			 */
			String json = "{ \"_id\":\"" + idDocumento + "\",\"_rev\":\""
					+ revisao + "\"," + dados;
			updateRegistro(url, json);
		} catch (IOException e) {
			System.out
					.println("Erro IOException - atualizarAluno()/AcessoBanco.java");
		}
		;

	}

	/**
	 * Função que irá efetivamente fazer a requisição Post para a atualização do
	 * registro no CouchDB. Os parâmetros recebidos são a url que deverá ser
	 * usada pelo Post e o json que será passado nesta requisição.
	 * 
	 * @param url
	 * @param json
	 */
	public void updateRegistro(String url, String json) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost requisicaoPost = new HttpPost(URI.create(url));
		HttpEntity input = new StringEntity(json, ContentType.APPLICATION_JSON);

		requisicaoPost.setEntity(input);
		HttpResponse response;
		try {
			System.out.println("POST : " + url + " - " + json);
			response = httpClient.execute(requisicaoPost);
			System.out.println(response);
			JOptionPane.showMessageDialog(null,
					"Registro alterado com sucesso!", "Cadastro",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			System.out
					.println("Erro na conexão! - updateRegistro()/AcessoBanco.java");
		}
	}

	/**
	 * Função utilizada para a remoção de um registro no banco do CouchDB. Ela
	 * recebe a url que será usada no HttpDelete e realiza a exclusão.
	 * 
	 * @param url
	 */
	public void removeRegistro(String url) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpDelete deleteRequest = new HttpDelete(url);
		HttpResponse response;
		try {
			System.out.println("DELETE : " + url);
			response = httpClient.execute(deleteRequest);
			System.out.println(response);
			JOptionPane.showMessageDialog(null,
					"Registro excluído com sucesso!", "Exclusão",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			System.out
					.println("Erro na conexão! - removeRegistro()/AcessoBanco.java");
		}
	}

	/**
	 * Função utilizada para se acrescentar informações no couchDB. É utilizada
	 * para criar um novo documento ou para criar um novo banco. O parâmetro
	 * recebido json é uma string em formato JSON contendo as informações de um
	 * novo registro ou vazio em caso da criação de um banco.
	 * 
	 * @param json
	 */
	public void setRegistro(String json) {
		DefaultHttpClient clienteHttp = new DefaultHttpClient();
		HttpPut requisicaoPUT;
		if (json.equals("")) {
			String url = getHost() + ":" + getPorta() + "/" + getNomebanco();
			requisicaoPUT = new HttpPut(URI.create(url));
			System.out.println("PUT : " + url);
		} else {
			String url = getHost() + ":" + getPorta() + "/" + getNomebanco()
					+ "/" + buscaId();
			requisicaoPUT = new HttpPut(URI.create(url));
			HttpEntity input = new StringEntity(json,
					ContentType.APPLICATION_JSON);
			requisicaoPUT.setEntity(input);
			System.out.println("PUT : " + url + " - " + json);
		}

		HttpResponse response;
		try {
			response = clienteHttp.execute(requisicaoPUT);
			System.out.println(response);
			JOptionPane.showMessageDialog(null,
					"Registro incluído com sucesso!", "Cadastro",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			System.out
					.println("Erro na conexão! - setRegistro()/AcessoBanco.java");
		}
	}

	/**
	 * 
	 * Função utilizada para fazer qualquer requisição Get com o banco Agenda do
	 * Aplicativo. No parâmetro recebido deve ser especificado uma Url válida
	 * para o CouchDB.
	 * 
	 * @param url
	 * @return
	 */
	public String getRegistro(String url) {
		HttpURLConnection conexao;
		BufferedReader rd;
		String linha;
		capturaJson = "";
		try {
			System.out.println("GET : " + url);
			this.urlConsulta = new URL(url);
			conexao = (HttpURLConnection) urlConsulta.openConnection();
			conexao.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(
					conexao.getInputStream()));
			while ((linha = rd.readLine()) != null) {
				capturaJson += linha;
			}
			rd.close();
		} catch (IOException e) {
			System.out
					.println("Erro na conexão! - getRegistro()/AcessoBanco.java");
			return null;
		}
		// retorna a string de retorno da requisição
		return capturaJson;
	}

	public String getLocalBusca() {
		return localBusca;
	}

	public void setLocalBusca(String localBusca) {
		this.localBusca = localBusca;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPorta() {
		return porta;
	}

	public void setPorta(String porta) {
		this.porta = porta;
	}

	public String getNomebanco() {
		return nomeBanco;
	}

	public void setNomeBanco(String nomeBanco) {
		this.nomeBanco = nomeBanco;
	}
}
