package M2fst.demo.service;


import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class IndexService {

    private DirectoryReader directoryReader;
    private IndexSearcher indexSearcher;
    private final IndexWriter indexWriter;
    private final QueryParser commentQueryParser;

    public IndexService() throws IOException {
        String indexDirectoryPath = "index";
        Path path = Paths.get(indexDirectoryPath);
        Directory index = FSDirectory.open(path);

        FrenchAnalyzer analyzer = new FrenchAnalyzer();
        // Cherche dans le champ "commentaire" d'une evaluation
        this.commentQueryParser = new QueryParser("commentaire", analyzer);

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        this.indexWriter = new IndexWriter(index, indexWriterConfig);

        if (DirectoryReader.indexExists(index)) {
            this.directoryReader = DirectoryReader.open(index);
            this.indexSearcher = new IndexSearcher(directoryReader);
        } else {
            // si l'index n'existe pas on le crée
            this.indexWriter.commit();
            this.directoryReader = DirectoryReader.open(index);
            this.indexSearcher = new IndexSearcher(directoryReader);
        }
    }

    /**
     * méthode qui permet d'ajouter/modifier un index
     * @param evaluationId id de l'évaluation indexée
     * @param commentaire commentaire indexé
     */
    public void indexerEvaluation(Integer evaluationId, String commentaire) {
        try {
            Document document = new Document();
            document.add(new StringField("evaluationId", String.valueOf(evaluationId), Field.Store.YES));
            document.add(new TextField("commentaire", commentaire, Field.Store.YES));
            this.indexWriter.updateDocument(new Term("evaluationId", String.valueOf(evaluationId)), document);
            this.indexWriter.commit();

            rafraichirLecteur();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * permet la suppression d'un index
     * @param evaluationId id de l'évaluation dont l'index doit être supprimé
     */
    public void supprimerEvaluation(Integer evaluationId){
        try {
            this.indexWriter.deleteDocuments(new Term("evaluationId", String.valueOf(evaluationId)));
            this.indexWriter.commit();
            rafraichirLecteur();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * méthode qui permet de rechercher des mots clés dans un index
     * @param motsCles mots clés recherchés
     * @return retourne la liste des identifiants d'évaluation ou l'on trouve le ou les mots clés
     */
    public List<Integer> rechercher(String motsCles) {
        try {
            Query query = this.commentQueryParser.parse(QueryParser.escape(motsCles));
            TopDocs results = this.indexSearcher.search(query, 100);

            List<Integer> evaluationsIds = new ArrayList<>();
            for (ScoreDoc hit : results.scoreDocs) {
                Document document = this.indexSearcher.storedFields().document(hit.doc);
                evaluationsIds.add(Integer.valueOf(document.get("evaluationId")));
            }
            return evaluationsIds;
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void rafraichirLecteur() throws IOException {
        DirectoryReader newDirectoryReader = DirectoryReader.openIfChanged(this.directoryReader);
        if (newDirectoryReader != null) {
            this.directoryReader.close();
            this.directoryReader = newDirectoryReader;
            this.indexSearcher = new IndexSearcher(this.directoryReader);
        }
    }
}
