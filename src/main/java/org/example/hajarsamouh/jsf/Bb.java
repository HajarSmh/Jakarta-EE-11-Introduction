package org.example.hajarsamouh.jsf;

import org.example.hajarsamouh.service.GenerateurReponse;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Portée view pour conserver l'état de la conversation
 * pendant plusieurs requêtes HTTP.
 */
@Named("bb")
@ViewScoped
public class Bb implements Serializable {

    /**
     * Rôle système attribué à l'API.
     */
    private String roleSysteme;

    /**
     * Indique si le rôle système peut encore être modifié.
     */
    private boolean roleSystemeChangeable = true;

    /**
     * Liste des rôles prédéfinis.
     */
    private List<SelectItem> listeRolesSysteme;

    /**
     * Style choisi pour la reformulation (pirate, verlan, sms, vieux).
     */
    private String style = GenerateurReponse.PIRATE;

    /**
     * Liste des styles proposés.
     */
    private List<SelectItem> listeStyles;

    /**
     * Dernière question posée par l'utilisateur.
     */
    private String question;

    /**
     * Dernière réponse de l'API.
     */
    private String reponse;

    /**
     * Historique de la conversation.
     */
    private StringBuilder conversation = new StringBuilder();

    /**
     * Service chargé de générer la réponse.
     */
    @Inject
    private GenerateurReponse generateurReponse;

    /**
     * Contexte JSF pour afficher les messages d'erreur.
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Constructeur requis pour le bean CDI.
     */
    public Bb() {
    }

    public String getRoleSysteme() {
        return this.roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public String getStyle() {
        return this.style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * Retourne les styles disponibles dans la liste déroulante.
     */
    public List<SelectItem> getStyles() {
        if (this.listeStyles == null) {
            this.listeStyles = new ArrayList<>();
            this.listeStyles.add(new SelectItem(GenerateurReponse.PIRATE, "Pirate"));
            this.listeStyles.add(new SelectItem(GenerateurReponse.VERLAN, "Verlan"));
            this.listeStyles.add(new SelectItem(GenerateurReponse.SMS, "Langage SMS"));
            this.listeStyles.add(new SelectItem(GenerateurReponse.VIEUX, "Vieux français"));
        }
        return this.listeStyles;
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

    /**
     * Envoie la question et affiche la réponse.
     *
     * @return null pour rester dans la même vue.
     */
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

        // Fige le rôle système au début de la conversation.
        if (this.conversation.isEmpty()) {
            this.roleSystemeChangeable = false;
        }

        // Génère la réponse à partir de la question.
        this.reponse = this.generateurReponse.genererReponse(
                this.question,
                this.roleSysteme,
                this.style
        );

        // Ajoute la question et la réponse à l'historique.
        afficherConversation();

        return null;
    }

    /**
     * Démarre un nouveau chat.
     *
     * @return "index" pour créer une nouvelle vue.
     */
    public String nouveauChat() {
        return "index";
    }

    /**
     * Ajoute la dernière question et sa réponse à l'historique.
     */
    private void afficherConversation() {
        this.conversation
                .append("== User:\n")
                .append(question)
                .append("\n== Serveur:\n")
                .append(reponse)
                .append("\n");
    }

    /**
     * Retourne les rôles disponibles dans la liste déroulante.
     */
    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();

            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Assistant")
            );

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Traducteur Anglais-Français")
            );

            role = """
                    Your are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    are you tell them the average price of a meal.
                    """;

            this.listeRolesSysteme.add(
                    new SelectItem(role, "Guide touristique")
            );
        }

        return this.listeRolesSysteme;
    }
}