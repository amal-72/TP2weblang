package ma.emsi.amal.tp2;

import ma.emsi.amal.tp2.llm.LlmClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class Bb implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;

    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    @Inject
    private LlmClient llmClient;

    @Inject
    private FacesContext facesContext;

    public Bb() {
    }

    public String getRoleSysteme() {
        return this.roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return this.roleSystemeChangeable;
    }

    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return this.reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    public String envoyer() {

        if (this.question == null || this.question.isBlank()) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Texte question vide",
                    "Il manque le texte de la question"
            );

            facesContext.addMessage(null, message);
            return null;
        }

        // Première question de la conversation
        if (this.conversation.isEmpty()) {

            // Figer la liste déroulante
            this.roleSystemeChangeable = false;

            // Ajouter le rôle système à la mémoire du LLM
            this.llmClient.setSystemRole(this.roleSysteme);
        }

        // Envoyer la question au LLM
        this.reponse = this.llmClient.chat(this.question);

        // Ajouter la question et la réponse à la conversation
        afficherConversation();

        return null;
    }

    public String nouveauChat() {
        return "index?faces-redirect=true";
    }
    private void afficherConversation() {
        this.conversation
                .append("== User:\n")
                .append(question)
                .append("\n== Assistant:\n")
                .append(reponse)
                .append("\n");
    }

    public List<SelectItem> getRolesSysteme() {

        if (this.listeRolesSysteme == null) {

            this.listeRolesSysteme = new ArrayList<>();

            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user types a question, you answer it.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Assistant")
            );

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user types a French text, you translate it into English.
                    If the user types an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Traducteur Anglais-Français")
            );

            role = """
                    You are a travel guide. If the user types the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    and the average price of a meal.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Guide touristique")
            );
        }

        return this.listeRolesSysteme;
    }
}
