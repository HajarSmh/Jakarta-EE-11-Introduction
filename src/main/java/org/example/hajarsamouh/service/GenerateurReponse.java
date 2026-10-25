package org.example.hajarsamouh.service;

import jakarta.enterprise.context.Dependent;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Dependent
public class GenerateurReponse implements Serializable {

    /** Styles disponibles. */
    public static final String PIRATE = "pirate";
    public static final String VERLAN = "verlan";
    public static final String SMS = "sms";
    public static final String VIEUX = "vieux";

    /** Un dictionnaire de remplacement par style (clés sans accents, en minuscules). */
    private static final Map<String, Map<String, String>> DICTIONNAIRES = new HashMap<>();

    /** Petite phrase ajoutée à la fin selon le style. */
    private static final Map<String, String> SIGNATURES = new HashMap<>();

    /** Nom affiché pour chaque style. */
    private static final Map<String, String> NOMS = new HashMap<>();

    static {
        DICTIONNAIRES.put(PIRATE, dico(
                "bonjour", "Ohé", "salut", "Ohé", "bonsoir", "Ohé",
                "ami", "moussaillon", "amis", "moussaillons", "copain", "moussaillon",
                "merci", "mille sabords, merci", "oui", "aye", "non", "que nenni",
                "argent", "doublons", "tresor", "butin", "mer", "grande bleue",
                "ocean", "grande bleue", "eau", "rhum", "cafe", "rhum", "boire", "siffler du rhum",
                "homme", "loup de mer", "femme", "sirène", "maison", "repaire",
                "ecole", "cale du navire", "professeur", "capitaine", "chef", "capitaine",
                "telephone", "longue-vue", "ordinateur", "grimoire maudit",
                "question", "énigme", "voiture", "galion", "super", "sacrément bon",
                "bien", "fichtrement bien", "je", "moi", "bateau", "navire"));

        DICTIONNAIRES.put(VERLAN, dico(
                "bonjour", "jourbon", "femme", "meuf", "mec", "keum", "fete", "teuf",
                "cafe", "féca", "bizarre", "zarbi", "francais", "céfran", "musique", "zicmu",
                "merci", "cimer", "fou", "ouf", "lourd", "relou", "louche", "chelou",
                "branche", "chébran", "enerve", "vénère", "pourri", "ripou",
                "mere", "reum", "pere", "reup", "frere", "reuf", "oui", "ouais",
                "tomber", "béton", "ami", "pote", "copain", "pote",
                "travail", "taf", "argent", "thunes"));

        DICTIONNAIRES.put(SMS, dico(
                "bonjour", "bjr", "bonsoir", "bsr", "salut", "slt", "beaucoup", "bcp",
                "merci", "mci", "pourquoi", "pk", "maintenant", "mnt", "message", "msg",
                "avec", "avc", "pas", "pa", "tout", "tt", "toujours", "tjrs", "quand", "qd",
                "demain", "2m1", "bien", "b1", "bientot", "b1to", "rendez", "rdv",
                "oui", "wi", "non", "nn", "je", "j", "tu", "t", "ami", "pote",
                "question", "kestion", "quoi", "koi", "comment", "komen", "cest", "c",
                "telephone", "tel", "ordinateur", "pc", "super", "sup", "aussi", "osi"));

        DICTIONNAIRES.put(VIEUX, dico(
                "bonjour", "Dieu vous garde", "salut", "Dieu vous garde",
                "bonsoir", "que la nuit vous soit douce",
                "merci", "grand merci, noble sire", "oui", "oïl", "non", "nenni",
                "ami", "compagnon", "amis", "compagnons", "copain", "compagnon",
                "monsieur", "messire", "madame", "dame", "beaucoup", "moult", "tres", "fort",
                "maison", "demeure", "argent", "deniers", "manger", "se restaurer",
                "toi", "vous", "question", "requête", "voiture", "carrosse",
                "telephone", "messager", "travail", "labeur", "professeur", "maître",
                "ordinateur", "engin diabolique", "maintenant", "à présent",
                "pourquoi", "pour quelle raison", "bien", "fort bien", "super", "fort plaisant"));

        SIGNATURES.put(PIRATE, " Arrr !");
        SIGNATURES.put(VERLAN, " C'est ouf !");
        SIGNATURES.put(SMS, " lol");
        SIGNATURES.put(VIEUX, " Par ma foi !");

        NOMS.put(PIRATE, "Pirate");
        NOMS.put(VERLAN, "Verlan");
        NOMS.put(SMS, "Langage SMS");
        NOMS.put(VIEUX, "Vieux français");
    }

    private boolean roleSystemeAffiche = false;

    /**
     * Version d'origine : analyse du type de message, sans style.
     */
    public String genererReponse(String question, String roleSysteme) {
        return genererReponse(question, roleSysteme, null);
    }

    /**
     * Analyse le type de message (comme avant) puis ajoute la question
     * reformulée dans le style choisi (pirate, verlan, sms, vieux).
     */
    public String genererReponse(String question, String roleSysteme, String style) {
        StringBuilder resultat = new StringBuilder();

        // Afficher le rôle choisi lors du premier échange
        if (!this.roleSystemeAffiche && roleSysteme != null) {
            resultat.append("Rôle sélectionné : ")
                    .append(roleSysteme.trim())
                    .append("\n\n");

            this.roleSystemeAffiche = true;
        }

        String texte = normaliser(question);
        String type;
        String reponse;

        if (contientUnMot(texte, "bonjour", "salut", "coucou", "hello", "bonsoir")) {
            type = "Salutation";
            reponse = "Bonjour ! Comment puis-je vous aider ?";
        } else if (contientUnMot(texte, "merci", "remercie")) {
            type = "Remerciement";
            reponse = "Avec plaisir ! N'hésitez pas si vous avez d'autres questions.";
        } else if (contientUnMot(texte, "a bientot", "au revoir", "adieu", "bye")) {
            type = "Au revoir";
            reponse = "À bientôt et bonne continuation !";
        } else if (texte.contains("?")
                || commenceParUnMot(texte,
                "qui", "quoi", "quand", "comment", "pourquoi",
                "ou", "quel", "quelle", "quels", "quelles",
                "est-ce")) {
            type = "Question";
            reponse = "Votre message ressemble à une question. "
                    + "Je vous invite à préciser votre demande.";
        } else {
            type = "Message général";
            reponse = "Votre message a bien été reçu. "
                    + "Pouvez-vous préciser ce que vous souhaitez ?";
        }

        resultat.append("Type détecté : ")
                .append(type)
                .append("\n")
                .append(reponse);

        // Nouveau : reformulation de la question dans le style choisi
        if (style != null && DICTIONNAIRES.containsKey(style)) {
            String traduit = traduire(question, DICTIONNAIRES.get(style));

            resultat.append("\n\nStyle ")
                    .append(NOMS.get(style))
                    .append(" : ")
                    .append(traduit.trim())
                    .append(SIGNATURES.get(style));
        }

        return resultat.toString();
    }

    private String normaliser(String texte) {
        if (texte == null) {
            return "";
        }

        String sansAccents = Normalizer.normalize(
                texte.toLowerCase(Locale.FRENCH),
                Normalizer.Form.NFD
        );

        return sansAccents
                .replaceAll("\\p{M}", "")
                .trim();
    }

    private boolean contientUnMot(String texte, String... mots) {
        for (String mot : mots) {
            if (texte.matches(".*\\b" + mot + "\\b.*")) {
                return true;
            }
        }
        return false;
    }

    private boolean commenceParUnMot(String texte, String... mots) {
        for (String mot : mots) {
            if (texte.matches("^" + mot + "\\b.*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remplace chaque mot trouvé dans le dictionnaire.
     * Les autres mots, la ponctuation et les espaces sont conservés.
     */
    private String traduire(String texte, Map<String, String> dictionnaire) {
        if (texte == null) {
            return "";
        }

        Matcher matcher = Pattern.compile("\\p{L}+").matcher(texte);
        StringBuilder sortie = new StringBuilder();

        while (matcher.find()) {
            String mot = matcher.group();
            String remplacement = dictionnaire.get(normaliser(mot));

            if (remplacement != null) {
                matcher.appendReplacement(sortie,
                        Matcher.quoteReplacement(respecterMajuscule(mot, remplacement)));
            }
        }
        matcher.appendTail(sortie);

        return sortie.toString();
    }

    /** Si le mot d'origine commence par une majuscule, le remplacement aussi. */
    private String respecterMajuscule(String original, String remplacement) {
        if (Character.isUpperCase(original.charAt(0)) && !remplacement.isEmpty()) {
            return Character.toUpperCase(remplacement.charAt(0)) + remplacement.substring(1);
        }
        return remplacement;
    }

    /** Construit un dictionnaire à partir d'une suite clé, valeur, clé, valeur... */
    private static Map<String, String> dico(String... paires) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i + 1 < paires.length; i += 2) {
            map.put(paires[i], paires[i + 1]);
        }
        return map;
    }
}