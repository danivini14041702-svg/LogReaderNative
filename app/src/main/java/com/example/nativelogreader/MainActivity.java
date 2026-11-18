package com.example.nativelogreader;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_LOG_FILE = 1;
    private static final String TAG = "AdMobDebug";

    private TextView tvSummary;
    private LinearLayout containerErros;
    private DrawerLayout drawerLayout;
    private SharedPreferences prefs;

    private AdView bannerAdView;
    private InterstitialAd mInterstitialAd;
    private int ultimoIndexClicado = -1;
    private AppOpenAd appOpenAd = null;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;
    private NativeAd nativeAd;

    // Controle de Ação Pendente (para fazer o app continuar o que ia fazer depois do anúncio)
    private Runnable acaoPendente = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("config", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", true);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, initializationStatus -> Log.d(TAG, "MobileAds inicializado"));

        carregarBannerPrincipal();
        carregarBannerMenu();
        carregarInterstitial();
        carregarAppOpenAd();
        carregarNativeAd();

        ErrorDatabase.sincronizarNuvem(this);

        drawerLayout = findViewById(R.id.drawerLayout);
        tvSummary = findViewById(R.id.tvSummary);
        containerErros = findViewById(R.id.containerErros);
        Switch switchTema = findViewById(R.id.switchTema);

        switchTema.setChecked(isDarkMode);
        findViewById(R.id.btnMenuLateral).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        switchTema.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // BOTÃO GEMINI COM ANÚNCIO E SIRENE
        EditText edtGemini = findViewById(R.id.edtGemini);
        findViewById(R.id.btnPerguntarGemini).setOnClickListener(v -> {
            String pergunta = edtGemini.getText().toString().trim();
            if (!pergunta.isEmpty()) {
                tocarSirene(); // TOCA A SIRENE
                mostrarAnuncioAntesDe(() -> abrirPesquisaIA(pergunta));
            } else {
                Toast.makeText(this, "Digite uma pergunta primeiro!", Toast.LENGTH_SHORT).show();
            }
        });

        // TODOS OS LINKS DO MENU COM ANÚNCIO E SIRENE
        findViewById(R.id.linkStackOverflow).setOnClickListener(v -> {
            tocarSirene();
            mostrarAnuncioAntesDe(() -> abrirEmCustomTab("https://stackoverflow.com/"));
        });
        findViewById(R.id.linkXDA).setOnClickListener(v -> {
            tocarSirene();
            mostrarAnuncioAntesDe(() -> abrirEmCustomTab("https://xdaforums.com/"));
        });
        findViewById(R.id.linkForumAndroid).setOnClickListener(v -> {
            tocarSirene();
            mostrarAnuncioAntesDe(() -> abrirEmCustomTab("https://android.stackexchange.com/"));
        });

        // BOTÃO COMO USAR COM ANÚNCIO E SIRENE
        findViewById(R.id.btnComoUsar).setOnClickListener(v -> {
            tocarSirene();
            mostrarAnuncioAntesDe(this::mostrarTutorial);
        });

        // BOTÃO COMPARTILHAR COM ANÚNCIO E SIRENE
        findViewById(R.id.btnCompartilharApp).setOnClickListener(v -> {
            tocarSirene();
            mostrarAnuncioAntesDe(() -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Conheça o Log Reader Ads! O app que traduz erros de Android.");
                startActivity(Intent.createChooser(intent, "Compartilhar App"));
            });
        });

        // BOTÃO SELECIONAR ARQUIVO COM ANÚNCIO E SIRENE
        findViewById(R.id.btnSelectFile).setOnClickListener(v -> {
            tocarSirene();
            mostrarAnuncioAntesDe(() -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_LOG_FILE);
            });
        });
    }

    // --- A MALDADE SUPREMA: FUNÇÃO QUE TOCA SIRENE ---
    private void tocarSirene() {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
            if (ringtone != null) {
                ringtone.play();
                // Para a sirene depois de 3 segundos para não fritar o alto-falante
                new android.os.Handler().postDelayed(ringtone::stop, 3000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao tocar sirene: " + e.getMessage());
        }
    }

    // --- FUNÇÃO QUE MOSTRA ANÚNCIO ANTES DE QUALQUER AÇÃO ---
    private void mostrarAnuncioAntesDe(Runnable acao) {
        acaoPendente = acao;
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    if (acaoPendente != null) {
                        acaoPendente.run();
                        acaoPendente = null;
                    }
                    carregarInterstitial(); // Já prepara o próximo
                }
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    if (acaoPendente != null) {
                        acaoPendente.run();
                        acaoPendente = null;
                    }
                }
            });
            mInterstitialAd.show(this);
        } else {
            // Se o Google não tiver anúncio pronto na hora, ele executa a ação direto
            acao.run();
            acaoPendente = null;
            // Tenta recarregar para a próxima
            carregarInterstitial();
        }
    }
    // ----------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
        mostrarAppOpenSeDisponivel();
    }

    private void carregarAppOpenAd() {
        if (isLoadingAd || isAdAvailable()) return;
        isLoadingAd = true;
        String adUnitId = "ca-app-pub-3940256099942544/9257395921";
        AdRequest request = new AdRequest.Builder().build();
        
        AppOpenAd.load(this, adUnitId, request, new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                appOpenAd = ad;
                isLoadingAd = false;
                Log.d(TAG, "✅ App Open carregado");
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                isLoadingAd = false;
            }
        });
    }

    private boolean isAdAvailable() { return appOpenAd != null; }

    private void mostrarAppOpenSeDisponivel() {
        if (isShowingAd || !isAdAvailable()) {
            carregarAppOpenAd();
            return;
        }
        isShowingAd = true;
        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                appOpenAd = null;
                isShowingAd = false;
                carregarAppOpenAd();
            }
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                appOpenAd = null;
                isShowingAd = false;
                carregarAppOpenAd();
            }
        });
        appOpenAd.show(this);
    }

    private void carregarBannerPrincipal() {
        bannerAdView = findViewById(R.id.adViewMain);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
    }

    private void carregarBannerMenu() {
        try {
            AdView bannerMenu = findViewById(R.id.adViewDrawerMain);
            if (bannerMenu != null) {
                bannerMenu.loadAd(new AdRequest.Builder().build());
            }
        } catch (Exception e) {
            Log.e(TAG, "Falha ao carregar banner do menu: " + e.getMessage());
        }
    }

    private void carregarInterstitial() {
        String adUnitId = "ca-app-pub-3940256099942544/1033173712";
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, adUnitId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
            }
        });
    }

    private void carregarNativeAd() {
        AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd ad) {
                        if (nativeAd != null) nativeAd.destroy();
                        nativeAd = ad;
                        Log.d(TAG, "✅ Native Ad carregado e disfarçado de erro!");
                    }
                })
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void adicionarNativeAdNaLista() {
        if (nativeAd == null) return;

        LayoutInflater inflater = getLayoutInflater();
        NativeAdView adView = (NativeAdView) inflater.inflate(R.layout.native_ad_layout, null);

        TextView headlineView = adView.findViewById(R.id.ad_headline);
        headlineView.setText(nativeAd.getHeadline());
        adView.setHeadlineView(headlineView);

        TextView bodyView = adView.findViewById(R.id.ad_body);
        if (nativeAd.getBody() == null) bodyView.setVisibility(View.INVISIBLE);
        else { bodyView.setText(nativeAd.getBody()); bodyView.setVisibility(View.VISIBLE); }
        adView.setBodyView(bodyView);

        Button callToActionView = adView.findViewById(R.id.ad_call_to_action);
        if (nativeAd.getCallToAction() == null) callToActionView.setVisibility(View.INVISIBLE);
        else { callToActionView.setText(nativeAd.getCallToAction()); callToActionView.setVisibility(View.VISIBLE); }
        adView.setCallToActionView(callToActionView);

        ImageView iconView = adView.findViewById(R.id.ad_app_icon);
        if (nativeAd.getIcon() == null) iconView.setVisibility(View.GONE);
        else { iconView.setImageDrawable(nativeAd.getIcon().getDrawable()); iconView.setVisibility(View.VISIBLE); }
        adView.setIconView(iconView);

        adView.setNativeAd(nativeAd);
        containerErros.addView(adView);
    }

    private void mostrarTutorial() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tutorial, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        dialogView.findViewById(R.id.btnEntendi).setOnClickListener(v -> dialog.dismiss());
    }

    private void abrirPesquisaIA(String pergunta) {
        String url = "https://www.google.com/search?q=" + Uri.encode(pergunta + " em termos leigos") + "&udm=50";
        abrirEmCustomTab(url);
    }

    private void abrirEmCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        builder.build().launchUrl(this, Uri.parse(url));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_LOG_FILE && resultCode == RESULT_OK && data != null) {
            processLogFile(data.getData());
        }
    }

    private void processLogFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) content.append(line).append("\n");
            reader.close();
            analyzeLog(content.toString());
        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void analyzeLog(String content) {
        containerErros.removeAllViews();
        ErrorDatabase.errosEncontrados.clear();
        
        adicionarNativeAdNaLista(); // O anúncio verde no topo

        List<ErrorPattern> knownErrors = ErrorDatabase.getKnownPatterns();
        int encontrados = 0;
        for (int i = 0; i < knownErrors.size(); i++) {
            ErrorPattern err = knownErrors.get(i);
            if (content.toLowerCase().contains(err.pattern.toLowerCase())) {
                ErrorDatabase.errosEncontrados.add(err);
                criarCartaoDeErro(err, ErrorDatabase.errosEncontrados.size() - 1);
                encontrados++;
            }
        }
        if (encontrados == 0) tvSummary.setText("Nenhum erro conhecido. Mas baixe o app aí em cima!");
        else tvSummary.setText("Encontramos " + encontrados + " erro(s):");
    }

    private void criarCartaoDeErro(ErrorPattern err, int index) {
        Button card = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setBackgroundColor(err.severity.equals("CRITICO") ? 0xFF8B0000 : 0xFFFF8C00);
        card.setTextColor(0xFFFFFFFF);
        card.setTextSize(16f);
        card.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        card.setPadding(30, 30, 30, 30);
        card.setText("🚨 " + err.pattern);

        card.setOnClickListener(v -> {
            ultimoIndexClicado = index;
            // AQUI TAMBÉM TOCA A SIRENE
            tocarSirene(); 
            mostrarAnuncioAntesDe(() -> irParaDetalhes(ultimoIndexClicado));
        });
        containerErros.addView(card);
    }

    private void irParaDetalhes(int index) {
        Intent intent = new Intent(this, ErrorDetailActivity.class);
        intent.putExtra("index", index);
        startActivity(intent);
    }
}
