package com.secure.documentwallet;

import java.util.Locale;

final class DocumentGroups {
    static final String ID = "ID / Permit";
    static final String CERTIFICATES = "Certificates";
    static final String CARDS = "Bank Cards";
    static final String DOCUMENTS = "Documents";

    private DocumentGroups() {}

    static String fromCategory(String category) {
        String value = category == null ? "" : category.toLowerCase(Locale.US);
        if (value.contains("identity") || value.contains("permit") || value.contains("licence") || value.equals("id / permit")) return ID;
        if (value.contains("certificate")) return CERTIFICATES;
        if (value.contains("financial") || value.contains("bank") || value.contains("card")) return CARDS;
        return DOCUMENTS;
    }

    static int icon(String group) {
        if (ID.equals(group)) return R.drawable.ic_id_permit;
        if (CERTIFICATES.equals(group)) return R.drawable.ic_certificate;
        if (CARDS.equals(group)) return R.drawable.ic_bank_card;
        return R.drawable.ic_documents;
    }

    static int color(String group) {
        if (ID.equals(group)) return Ui.SOFT;
        if (CERTIFICATES.equals(group)) return 0xfffff6df;
        if (CARDS.equals(group)) return 0xffe8f6fc;
        return 0xffe5f2eb;
    }
}
