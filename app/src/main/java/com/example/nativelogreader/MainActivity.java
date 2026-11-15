private void criarCartaoDeErro(ErrorPattern err, int index) { // Adicionado o 'index'
    Button card = new Button(this);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, 24);
    card.setLayoutParams(params);
    
    if (err.severity.equals("CRITICO")) {
        card.setBackgroundColor(Color.parseColor("#8B0000"));
    } else {
        card.setBackgroundColor(Color.parseColor("#B8860B"));
    }
    
    card.setTextColor(Color.WHITE);
    card.setTextSize(16f);
    card.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
    card.setPadding(30, 30, 30, 30);
    card.setText("🚨 " + err.title);

    card.setOnClickListener(v -> {
        Intent intent = new Intent(this, ErrorDetailActivity.class);
        intent.putExtra("index", index); // Envia qual erro foi clicado
        startActivity(intent);
    });

    containerErros.addView(card);
}
