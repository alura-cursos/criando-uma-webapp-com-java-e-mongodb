package br.com.alura.escolalura.repositorys;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;

import br.com.alura.escolalura.codecs.AlunoCodec;
import br.com.alura.escolalura.models.Aluno;

@Repository
public class AlunoRepository {
	
	private MongoClient cliente;
	private MongoDatabase bancaDeDados;
	
	private void criarConexao() {
		Codec<Document> codec = MongoClient.getDefaultCodecRegistry().get(Document.class);
		
		AlunoCodec alunoCodec = new AlunoCodec(codec);
		
		CodecRegistry registro = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs(alunoCodec));
		
		MongoClientOptions opcoes = MongoClientOptions.builder().codecRegistry(registro).build();
		
		this.cliente = new MongoClient("localhost:27017", opcoes);
		this.bancaDeDados = cliente.getDatabase("test");
		
	}
	
	public void salvar(Aluno aluno){
		
		criarConexao();
		MongoCollection<Aluno> alunos = this.bancaDeDados.getCollection("alunos", Aluno.class);
		if (aluno.getId() == null) {
			alunos.insertOne(aluno);
		}else{
			alunos.updateOne(Filters.eq("_id", aluno.getId()), new Document("$set", aluno));
		}
		
		fecharConexao();
	}

	
	
	public List<Aluno> obterTodosAlunos(){
		criarConexao();
		MongoCollection<Aluno> alunos = this.bancaDeDados.getCollection("alunos", Aluno.class);
		
		MongoCursor<Aluno> resultados = alunos.find().iterator();
		
		List<Aluno> alunosEncontrados = popularAlunos(resultados);
		fecharConexao();
		
		return alunosEncontrados;
		
	}
	
	public Aluno obterAlunoPor(String id){
		criarConexao();
		MongoCollection<Aluno> alunos = this.bancaDeDados.getCollection("alunos", Aluno.class);
		Aluno aluno = alunos.find(Filters.eq("_id", new ObjectId(id))).first();
		
		return aluno;
		
	}

	public List<Aluno> pesquisarPor(String nome) {
		criarConexao();
		MongoCollection<Aluno> alunoCollection = this.bancaDeDados.getCollection("alunos" , Aluno.class);
		MongoCursor<Aluno> resultados = alunoCollection.find(Filters.eq("nome", nome), Aluno.class).iterator();
		List<Aluno> alunos = popularAlunos(resultados);
		
		fecharConexao();
		
		return alunos;
	}

	private void fecharConexao() {
		this.cliente.close();
	}
	
	private List<Aluno> popularAlunos(MongoCursor<Aluno> resultados){
		List<Aluno> alunos = new ArrayList<>();
		while(resultados.hasNext()){
			alunos.add(resultados.next());
		}
		return alunos;
	}

	public List<Aluno> pesquisarPor(String classificacao, double nota) {
		criarConexao();
		
		MongoCollection<Aluno> alunoCollection = this.bancaDeDados.getCollection("alunos", Aluno.class);
		
		MongoCursor<Aluno> resultados = null;
		
		if (classificacao.equals("reprovados")) {
			resultados = alunoCollection.find(Filters.lt("notas", nota)).iterator();
		}else if(classificacao.equals("aprovados")){
			resultados = alunoCollection.find(Filters.gte("notas", nota)).iterator();
		}
		
		List<Aluno> alunos = popularAlunos(resultados);
		
		fecharConexao();
		
		return alunos;
		
	}

	public List<Aluno> pesquisaPorGeolocalizacao(Aluno aluno) {
		criarConexao();
		MongoCollection<Aluno> alunoCollection = this.bancaDeDados.getCollection("alunos", Aluno.class);
		
		alunoCollection.createIndex(Indexes.geo2dsphere("contato"));
		
		List<Double> coordinates = aluno.getContato().getCoordinates();
		Point pontoReferencia = new Point(new Position(coordinates.get(0), coordinates.get(1)));
		
		MongoCursor<Aluno> resultados = alunoCollection.find(Filters.nearSphere("contato", pontoReferencia, 2000.0, 0.0)).limit(2).skip(1).iterator();
		
		List<Aluno> alunos = popularAlunos(resultados);
		
		fecharConexao();
		return alunos;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
