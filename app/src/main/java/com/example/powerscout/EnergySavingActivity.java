package com.example.powerscout;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EnergySavingActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private TipsAdapter tipsAdapter;
    private List<Tip> tips;
    private int currentTipIndex = 0;
    private SharedPreferences sharedPreferences;
    private EditText searchTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy_saving);
        setupNavigationDrawer();
        // Initialize Views
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        searchTips = findViewById(R.id.searchTips);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sharedPreferences = getSharedPreferences("PowerScoutPrefs", Context.MODE_PRIVATE);
        tips = new ArrayList<>();

        loadSampleTips();
        tipsAdapter = new TipsAdapter(tips, this);
        recyclerView.setAdapter(tipsAdapter);

        swipeRefresh.setOnRefreshListener(() -> {
            fetchTips();
            swipeRefresh.setRefreshing(false);
        });

        searchTips.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTips(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void loadSampleTips() {
        tips.add(new Tip("Energy Saving at Home", "Dr. Green",
                "https://media.licdn.com/dms/image/v2/D5612AQFAmKdYUUq4-w/article-cover_image-shrink_720_1280/article-cover_image-shrink_720_1280/0/1658486565678?e=1746662400&v=beta&t=KHtxvcyL951SvU1Evr0MTymf5ppQFVygzdGUlBDY03k",
                "Save energy by using efficient appliances.", 5,
                "https://www.iea.org/topics/saving-energy"));

        tips.add(new Tip("Use LED Bulbs", "Eco Expert",
                "https://miro.medium.com/v2/resize:fit:1100/format:webp/1*5U8k11KBvOaNSb_o4tXwdQ.jpeg",
                "LED bulbs use less energy and last longer.", 3,
                "https://www.energy.gov/energysaver/led-lighting"));

        tips.add(new Tip("Turn Off Unused Devices", "Sustainable Guru",
                "https://www.pressac.com/wp-content/uploads/2023/01/pressac-22-ways-of-saving-energy-at-your-workplace-266x266.png.webp",
                "Switch off devices when not in use.", 7,
                "https://www.wsj.com/articles/SB10001424052970204781804577267304080904804"));

        tips.add(new Tip("Unplug Chargers", "Green Planet",
                "https://apicms.thestar.com.my/uploads/images/2024/09/25/thumbs/700/2930622.webp",
                "Unplug chargers when not in use to avoid energy waste.", 4,
                "https://www.activesustainability.com/climate-change/learnsustainability-unplugging-the-phone-charger/#:~:text=A%20small%20gesture%20like%20unplugging,pocket%20in%20the%20light%20bill"));

        tips.add(new Tip("Use Smart Power Strips", "Tech Savvy",
                "https://oppdthewire.com/wp-content/uploads/2019/03/EE_Advanced-Power-Strips_homepage-680x510.jpg",
                "Smart power strips cut off power when devices are not in use.", 6,
                "https://www.energy.gov/energysaver/articles/save-energy-your-household-smart-power-strip"));

        tips.add(new Tip("Optimize Refrigerator Settings", "Eco Living",
                "https://cdn11.bigcommerce.com/s-fhnch/images/stencil/1280w/uploaded_images/cover-how-many-watts-does-a-refrigerator-use.png?t=1721011530",
                "Set your fridge to 37°F and your freezer to 0°F for efficiency.", 5,
                "https://shop.haierindia.com/blog/optimize-refrigerator-energy-efficiency/#:~:text=Optimal%20Temperature%20Range,C%20(0%C2%B0F)."));

        tips.add(new Tip("Use Natural Light", "Solar Enthusiast",
                "https://www.apge.com/hubfs/APG%26E---The-Benefits-Of-Natural-Light-For-Healthy-Homes.jpg",
                "Make use of natural daylight to reduce lighting costs.", 4,
                "https://danpal.com/environmental/the-energy-savings-of-daylighting/"));

        tips.add(new Tip("Reduce Water Heater Temperature", "Efficiency Pro",
                "https://cdn.prod.website-files.com/6361339b831c262ea482dcfa/6501e2a14cc4e63741c688a5_adjusting%20the%20temperature%20of%20a%20water%20heater.jpg",
                "Lower water heater temperature to 120°F to save energy.", 6,
                "https://www.energy.gov/energysaver/do-it-yourself-savings-project-lower-water-heating-temperature"));

        tips.add(new Tip("Seal Air Leaks", "Home Energy Expert",
                "https://chariot2020prd.wpenginepowered.com/wp-content/uploads/caulking-windows.jpg",
                "Seal gaps around doors and windows to prevent energy loss.", 7,
                "https://www.energy.gov/energysaver/air-sealing-your-home"));

        tips.add(new Tip("Wash Clothes in Cold Water", "Sustainable Living",
                "https://www.bhg.com/thmb/b6a4311oSS0stM0uMZks35Z_SdQ=/750x0/filters:no_upscale():max_bytes(150000):strip_icc():format(webp)/Cold-Water-Laundry-bc9ee4b5b8274c86830381a87ba9db31.jpg",
                "Use cold water for laundry to save on heating costs.", 5,
                "https://www.cleaninginstitute.org/industry-priorities/outreach/cold-water-saves#:~:text=About%2090%25%20of%20the%20energy%20used%20by%20the%20washing%20machine,as%20switching%20to%20cool%20water."));

        tips.add(new Tip("Use Ceiling Fans Wisely", "Climate Aware",
                "https://www.valuelights.co.uk/media/wysiwyg/Kitchen-Sketch-1920x812_3_1_1.jpg",
                "Ceiling fans help circulate air efficiently, reducing AC usage.", 6,
                "https://www.atempheating.com/blog/2024/september/how-a-ceiling-fan-can-save-you-money-on-your-pow/"));

        tips.add(new Tip("Install Solar Panels", "Renewable Future",
                "https://jebicon.com/wp-content/uploads/2023/06/installing-solar-panels.webp",
                "Solar panels provide sustainable energy and reduce bills.", 10,
                "https://contendresolar.com/blog/how-solar-panels-reduce-your-electricity-bills#:~:text=By%20installing%20solar%20panels%20on,electricity%20from%20your%20utility%20company."));
    }

    private void saveFavoriteTip() {
        Tip favoriteTip = tips.get(currentTipIndex);
        List<Tip> favoriteTips = getFavoriteTips();

        if (!favoriteTips.contains(favoriteTip)) {
            favoriteTips.add(favoriteTip);
            sharedPreferences.edit().putString("favorite_tips", new Gson().toJson(favoriteTips)).apply();
            Toast.makeText(this, "Saved as Favorite!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Tip already saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Tip> getFavoriteTips() {
        String json = sharedPreferences.getString("favorite_tips", "[]");
        Type type = new TypeToken<List<Tip>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    private void fetchTips() {
        Toast.makeText(this, "Fetching new tips...", Toast.LENGTH_SHORT).show();
    }

    private void filterTips(String query) {
        List<Tip> filteredList = new ArrayList<>();
        for (Tip tip : tips) {
            if (tip.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(tip);
            }
        }
        tipsAdapter.filterList(filteredList);
    }


}
