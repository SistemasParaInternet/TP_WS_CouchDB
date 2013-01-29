package gui;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import aplicacao.AcessoBanco;
import aplicacao.Contato;

import tratador_eventos.TratadorEventosEditar;

public class JanelaEditar extends JDialog{
	/**
	 */
	private static final long serialVersionUID = 1L;
	private JPanel painelPrincipal;
	private JLabel edicao = new JLabel("Edição");
	private JLabel nome = new JLabel("Nome: ");
	private JLabel apelido = new JLabel("Apelido");
	private JLabel telefoneResidencial = new JLabel("Telefone Residencial: ");
	private JLabel telefoneCelular = new JLabel("Telefone Celular: ");
	private JLabel cidade = new JLabel("Cidade: ");
	private JLabel estado = new JLabel("Estado: ");
	private JTextField textoNome = new JTextField(50);
	private JTextField textoApelido = new JTextField(30);
	private JTextField textoTelRes = new JTextField(12);
	private JTextField textoTelCel = new JTextField(12);
	private JTextField textoCidade = new JTextField(30);
	private String[] estados = {"AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG","PR","PB","PA","PE",
			"PI","RJ","RN","RS","RO","RR","SC","SE","SP","TO"};
	private JComboBox comboEstados = new JComboBox(estados);
	private JButton botaoSalvar = new JButton("Salvar");
	private JButton botaoCancelar = new JButton("Cancelar");
	private JPanel botoes = new JPanel();
	private TratadorEventosEditar tratadorEventosEditar;
	private AcessoBanco acessoBanco;
	private Contato contato;
	private int numeroDocumento;
	
	public JanelaEditar(AcessoBanco acessoBanco, Contato contato){
		this.acessoBanco = acessoBanco;
		this.contato = contato;
		this.numeroDocumento = contato.getId();
		setTitle("Editar Contato");
		setSize(300, 350);
	}

	public void montarJanelaEditar(){
		tratadorEventosEditar = new TratadorEventosEditar(acessoBanco,this);
		painelPrincipal = new JPanel();
		
		MigLayout migLayout = new MigLayout("wrap 3");
		painelPrincipal.setLayout(migLayout);
		
		botoes.add(botaoCancelar);
		botoes.add(botaoSalvar);
		
		edicao.setFont(new Font("Times New Roman", Font.BOLD, 20));
		painelPrincipal.add(edicao,"gaptop 10, spanx 2, wrap");
		painelPrincipal.add(nome, "gaptop 10");
		painelPrincipal.add(textoNome, "wrap");
		painelPrincipal.add(apelido);
		painelPrincipal.add(textoApelido,"wrap");
		painelPrincipal.add(telefoneResidencial);
		painelPrincipal.add(textoTelRes, "wrap");
		painelPrincipal.add(telefoneCelular);
		painelPrincipal.add(textoTelCel, "wrap");
		painelPrincipal.add(cidade);
		painelPrincipal.add(textoCidade, "wrap");
		painelPrincipal.add(estado);
		painelPrincipal.add(comboEstados, "wrap");
		painelPrincipal.add(botoes, "gapleft 70, spanx 2");

		textoNome.setText((String) contato.getNome());
		textoApelido.setText((String) contato.getApelido());
		textoTelRes.setText((String) contato.getTelefoneResidencial());
		textoTelCel.setText((String) contato.getTelefoneCelular());
		textoCidade.setText((String) contato.getCidade());
		int i;
		for (i=0;i<estados.length;i++)
			if (estados[i].equals(contato.getEstado()))
				break;
		comboEstados.setSelectedIndex(i);
		add(painelPrincipal);
		
		botaoSalvar.addActionListener(tratadorEventosEditar);
		botaoCancelar.addActionListener(tratadorEventosEditar);

		repaint();
		setLocationRelativeTo(null);
		setResizable(false);
		setModal(true);
		setVisible(true);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}
	
	public JComboBox getComboEstados() {
		return comboEstados;
	}

	public int getNumeroDocumento() {
		return numeroDocumento;
	}

	public JTextField getTextoNome() {
		return textoNome;
	}

	public void setTextoNome(JTextField textoNome) {
		this.textoNome = textoNome;
	}

	public JTextField getTextoApelido() {
		return textoApelido;
	}

	public JTextField getTextoTelRes() {
		return textoTelRes;
	}

	public JTextField getTextoTelCel() {
		return textoTelCel;
	}

	public JTextField getTextoCidade() {
		return textoCidade;
	}

	public JButton getBotaoSalvar() {
		return botaoSalvar;
	}

	public JButton getBotaoCancelar() {
		return botaoCancelar;
	}

	public JLabel getNome() {
		return nome;
	}

	public void setNome(JLabel nome) {
		this.nome = nome;
	}
	
}
