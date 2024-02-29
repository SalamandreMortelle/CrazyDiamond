package CrazyDiamond.Model;

import java.util.ArrayDeque;

public abstract class Commande {

    public static ArrayDeque<Commande> HistoriqueCommandesExecutees = new ArrayDeque<>(10) ;
    public static ArrayDeque<Commande> HistoriqueCommandesAnnulees  = new ArrayDeque<>(10) ;
    private boolean est_repetable = false ;

    private static boolean retablir_en_cours = false ;

    public static void effacerHistoriques() {
        HistoriqueCommandesExecutees.clear();
        HistoriqueCommandesAnnulees.clear();
    }
    public static void annulerDerniereCommande() {

//        System.out.println("===================> EXECUTES (avant annulerDerniereCommande):");
//        System.out.println(HistoriqueCommandesExecutees);
//        System.out.println("===================> ANNULEES (avant annulerDerniereCommande) :");
//        System.out.println(HistoriqueCommandesAnnulees);
//        System.out.println("");

        if (HistoriqueCommandesExecutees.size()==0)
            return ;

        Commande derniere_commande = HistoriqueCommandesExecutees.pop() ;
        derniere_commande.annuler();
        HistoriqueCommandesAnnulees.push(derniere_commande);


//        HistoriqueCommandesExecutees.peekLast().annuler();
//        HistoriqueCommandesAnnulees.addLast(HistoriqueCommandesExecutees.peekLast());
//        HistoriqueCommandesExecutees.removeLast() ;
    }

    public static void retablirCommande() {
        if (HistoriqueCommandesAnnulees.size()==0)
            return;

        retablir_en_cours = true ;
        Commande cmd_a_retablir = HistoriqueCommandesAnnulees.pop() ;

        cmd_a_retablir.executer();
        retablir_en_cours = false ;

//        HistoriqueCommandesExecutees.addLast(HistoriqueCommandesAnnulees.peekLast());
//        HistoriqueCommandesAnnulees.removeLast() ;
    }

    public static void repeterDerniereCommande() {

        if (HistoriqueCommandesExecutees.size()==0)
            return ;
        if (!HistoriqueCommandesExecutees.peekFirst().estRepetable())
            return;

        HistoriqueCommandesExecutees.peekFirst().executer();
//        HistoriqueCommandesExecutees.peekLast().enregistrer();

    }

    public static void convertirDistancesHistoriques(double facteur_conversion) {
        HistoriqueCommandesExecutees.forEach(c -> c.convertirDistances(facteur_conversion));
        HistoriqueCommandesAnnulees.forEach(c -> c.convertirDistances(facteur_conversion));
    }

    protected void convertirDistances(double facteur_conversion) { }

    public abstract void executer() ;
    public abstract void annuler() ;

    public boolean estRepetable() {
        return est_repetable ;
    }

    public void definirRepetable(boolean est_repetable) {
        this.est_repetable = est_repetable ;
    }

    public void enregistrer() {

        HistoriqueCommandesExecutees.push(this);

        if (!retablir_en_cours)
            HistoriqueCommandesAnnulees.clear();

    }
    protected void desenregistrer() {
        if (HistoriqueCommandesExecutees.size()==0)
            return;

        if (HistoriqueCommandesExecutees.peekFirst()==this)
            HistoriqueCommandesExecutees.pop();
    }


}
