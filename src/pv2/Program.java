package pv2;

/* Uzeti implementaciju klasa 'Karta' i 'Spil' iz prvog zadatka i adaptirati ih
 * tako da se mogu koristiti od strane vise procesa istovremeno.
 *
 * Napraviti i pokrenuti 12 niti koje predstavljaju igrace. Svaka nit uzima
 * jednu kartu sa vrha spila i smesta je u svoje privatno polje. Potom tu kartu
 * stavlja na talon (videti ispod) i ceka da to urade i svi ostali igraci.
 *
 * Kada su svi igraci stavili svoje karte na talon, nastavljaju izvrsavanje.
 * Svako samostalno proverava da li je imao najjacu kartu i stampa prigodnu
 * poruku o tome. Moze biti vise igraca sa najjacom kartom, gleda se samo rang
 * karte kao i u prethodnim zadacima.
 *
 * Implementirati klasu 'Talon' koja ima sledece metode i koristiti je za
 * sinhronizaciju igraca:
 *
 *   void staviKartu(Karta)   - pomocu koje igrac stavlja kartu na talon
 *   void cekajOstale()       - blokira nit dok se na talon ne stavi 12 karata
 *                              ovaj metod baca InterruptedException ako neko
 *                              prekine nit u toku ovog cekanja
 *   boolean jeNajjaca(Karta) - utvrdjuje da li je prosledejna karta najjaca
 *
 * Glavna nit kreira spil i talon, pokrece sve ostale niti, posle cega zavrsava
 * svoj rad.
 */

import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

class Program {

    static Talon t;
    static SpilEnum s;

    enum BojaEnum {

        PIK(0xDCA0), KARO(0xDCC0), HERC(0xDCB0), TREF(0xDCD0);

        private final int vrednost;

        private BojaEnum(int vrednost) {
            this.vrednost = vrednost;
        }

        public int getVrednost() {
            return vrednost;
        }
    }

    enum RangEnum {

        DVA(2), TRI(3), CETIRI(4), PET(5),
        SEST(6), SEDAM(7), OSAM(8),
        DEVET(9), DESET(10), ZANDAR(11),
        KRALJICA(13), KRALJ(14), KEC(1);

        private final int vrednost;

        private RangEnum(int vrednost) {
            this.vrednost = vrednost;
        }

        public int getVrednost() {
            return vrednost;
        }
    }

    static class KartaEnum {

        private final BojaEnum boja;
        private final RangEnum rang;

        public KartaEnum(BojaEnum boja, RangEnum rang) {
            if (boja == null) {
                throw new IllegalArgumentException("boja");
            }
            if (rang == null) {
                throw new IllegalArgumentException("rang");
            }
            this.boja = boja;
            this.rang = rang;
        }


        public KartaEnum(boolean uBoji) {
            if (uBoji) {
                this.boja = BojaEnum.HERC;
            } else {
                this.boja = null;
            }
            this.rang = null; // Karta bez ranga je dzoker
        }

        public BojaEnum getBoja() {
            return boja;
        }

        public RangEnum getRang() {
            return rang;
        }

        @Override
        public int hashCode() {
            if (rang == null) {
                return 0;
            }
            int rezultat = boja.ordinal() + 1;
            rezultat = rezultat * 17 + rang.ordinal() + 1;
            return rezultat;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null) {
                return false;
            }
            if (getClass() != object.getClass()) {
                return false;
            }
            KartaEnum that = (KartaEnum) object;
            if (!Objects.equals(this.rang, that.rang)) {
                return false;
            }
            if (rang == null) {
                return (this.boja == null) == (that.boja == null);
            }
            if (!Objects.equals(this.boja, that.boja)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            if (rang == null && boja != null) {
                return "\uD83C\uDCDF"; // Dzoker u boji
            }
            if (rang == null && boja == null) {
                return "\uD83C\uDCCF"; // Dzoker bez boje
            }
            return "\uD83C" + (char) (boja.getVrednost() + rang.getVrednost());
        }
    }

    static class SpilEnum {

        private final List<KartaEnum> karte = new ArrayList<>(54);
        private final Random random;

        public SpilEnum() {
            this(3347);
        }

        public SpilEnum(long seed) {
            random = new Random(seed);
            for (BojaEnum boja : BojaEnum.values()) {
                for (RangEnum rang : RangEnum.values()) {
                    karte.add(new KartaEnum(boja, rang));
                }
            }
            karte.add(new KartaEnum(true)); // Dzoker u boji
            karte.add(new KartaEnum(false)); // Dzoker bez boje
        }

        public int velicina() {
            return karte.size();
        }

        public synchronized KartaEnum uzmiOdGore() {
            return karte.remove(karte.size() - 1);
        }

        public KartaEnum uzmiOdDole() {
            return karte.remove(0);
        }

        public KartaEnum uzmiIzSredine() {
            return karte.remove(random.nextInt(karte.size()));
        }

        public void staviGore(KartaEnum karta) {
            karte.add(karta);
        }

        public void staviDole(KartaEnum karta) {
            karte.add(0, karta);
        }

        public void staviUSredinu(KartaEnum karta) {
            karte.add(random.nextInt(karte.size() + 1), karta);
        }

        public synchronized void promesaj() {
            Collections.shuffle(karte, random);
        }
    }

    class IgracEnum implements Comparable<IgracEnum> {

        private final String ime;
        private final List<KartaEnum> karte;
        private boolean aktivan;

        public IgracEnum(String ime) {
            if (ime == null) {
                throw new IllegalArgumentException("ime");
            }
            this.ime = ime;
            this.karte = new ArrayList<>();
            aktivan = true;
        }

        public String getIme() {
            return ime;
        }

        public void dodajKartu(KartaEnum karta) {
            karte.add(karta);
        }

        public boolean getAktivan() {
            return aktivan;
        }

        public void setAktivan(boolean aktivan) {
            this.aktivan = aktivan;
        }

        @Override
        public int hashCode() {
            return ime.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null) {
                return false;
            }
            if (getClass() != object.getClass()) {
                return false;
            }
            IgracEnum that = (IgracEnum) object;
            return Objects.equals(this.ime, that.ime);
        }

        @Override
        public String toString() {
            String opis = karte.stream()
                    .map(KartaEnum::toString)
                    .collect(Collectors.joining());
            return String.format("%s %s %s", ime, aktivan ? ":)" : "  ", opis);
        }

        private static Comparator<RangEnum> KOMPARATOR_RANGOVA =
                Comparator.nullsLast(Comparator.naturalOrder());
        private static Comparator<KartaEnum> KOMPARATOR_KARATA =
                Comparator.comparing(KartaEnum::getRang, KOMPARATOR_RANGOVA);

        @Override
        public int compareTo(IgracEnum that) {
            // Igraci se porede po kolicini i jacini izvucenih karata
            int result = this.karte.size() - that.karte.size();
            for (int i = 0; i < karte.size() && result == 0; i++) {
                result = Objects.compare(
                        this.karte.get(i),
                        that.karte.get(i),
                        KOMPARATOR_KARATA);
            }
            return result;
        }
    }

    static class Talon {

        private KartaEnum najjaca = null;
        private List<KartaEnum> karte = new ArrayList<>();

        synchronized void staviKartu(KartaEnum karta) {
            karte.add(karta);
            if(najjaca == null) {
                najjaca = karta;
                return;
            }
            int rang = (karta.rang == null) ? 15 : karta.rang.ordinal();
            int najjacaRang = (najjaca.rang == null) ? 15 : najjaca.rang.ordinal();
            if(rang > najjacaRang) {
                najjaca = karta;
            }
        }

        private synchronized boolean sviStavili() {
            return karte.size() == 12;
        }


        void cekajOstale() throws InterruptedException {
            while (!sviStavili());
        }

        synchronized boolean jeNajjaca(KartaEnum k) {
            int kVrednost = (k.rang == null) ? 15 : k.rang.ordinal();
            int najjacaVrednost = (najjaca.rang == null) ? 15 : najjaca.rang.ordinal();
            return kVrednost == najjacaVrednost;
        }

    }


    static class Igrac extends Thread {

        private KartaEnum k = null;

        @Override
        public void run() {
            k = s.uzmiOdGore();
            t.staviKartu(k);
            try {
                t.cekajOstale();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(t.jeNajjaca(k)) {
                System.out.println("Nit " + this.getName() + " ima najjacu kartu: " + k.toString());
            }
        }
    }


    public static void main(String[] args) {
        List<Igrac> igraci = new ArrayList<>();
        t = new Talon();
        s = new SpilEnum();
        s.promesaj();
        for (int i = 0; i < 12; i++) {
            Igrac igr = new Igrac();
            igraci.add(igr);
            igr.start();
        }
    }
}
