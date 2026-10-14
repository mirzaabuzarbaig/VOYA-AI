package com.voyaai.backend.service;

import com.voyaai.backend.dto.PlaceItemDto;
import java.util.*;

public class CuratedDestinations {

    private static final Map<String, List<PlaceItemDto>> REGISTRY = new HashMap<>();

    static {
        // ═══════════════════════════════════════════════════════════════
        // DUBAI, UAE
        // ═══════════════════════════════════════════════════════════════
        List<PlaceItemDto> dubai = new ArrayList<>();
        dubai.add(new PlaceItemDto(
                "dubai-burj-khalifa", "Burj Khalifa", "Landmark",
                "Downtown Dubai, Dubai, UAE", "4.8",
                "https://upload.wikimedia.org/wikipedia/en/9/93/Burj_Khalifa.jpg",
                "World's tallest building soaring at 828m with panoramic observation decks on levels 124, 125, and 148.",
                "https://www.burjkhalifa.ae", "1 Sheikh Mohammed bin Rashid Blvd, Downtown Dubai",
                25.1972, 55.2744, 120, 179.0, "Downtown Dubai"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-mall", "The Dubai Mall & Dubai Aquarium", "Shopping & Entertainment",
                "Downtown Dubai, Dubai, UAE", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/The_Dubai_Mall_%282710189086%29.jpg/800px-The_Dubai_Mall_%282710189086%29.jpg",
                "Premier retail, dining, and entertainment destination featuring an Olympic-sized ice rink and massive underwater aquarium.",
                "https://thedubaimall.com", "Financial Centre Road, Downtown Dubai",
                25.1985, 55.2796, 180, 150.0, "Downtown Dubai"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-fountain", "The Dubai Fountain", "Sightseeing",
                "Downtown Dubai, Dubai, UAE", "4.8",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Dubai_fountain_at_night.jpg/800px-Dubai_fountain_at_night.jpg",
                "Captivating choreographed water fountain spectacle set to music on the 30-acre Burj Khalifa Lake.",
                "https://thedubaimall.com/en/entertain/the-dubai-fountain", "Sheikh Mohammed bin Rashid Blvd, Downtown Dubai",
                25.1950, 55.2760, 45, 0.0, "Downtown Dubai"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-time-out-market", "Time Out Market Dubai", "Food",
                "Downtown Dubai, Dubai, UAE", "4.6",
                null,
                "Curated waterfront gastronomic food hall in Souk Al Bahar showcasing 17 top local homegrown chef concepts.",
                "https://www.timeoutmarket.com/dubai", "Souk Al Bahar, Downtown Dubai",
                25.1955, 55.2778, 75, 120.0, "Downtown Dubai"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-museum-of-future", "Museum of the Future", "Museum",
                "Trade Centre 2, Dubai, UAE", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Museum_of_the_Future_%28Dubai%29_in_2022.jpg/800px-Museum_of_the_Future_%28Dubai%29_in_2022.jpg",
                "Architectural wonder exploring cutting-edge science, sustainability, space exploration, and human potential in 2071.",
                "https://museumofthefuture.ae", "Sheikh Zayed Rd, Trade Centre 2",
                25.2253, 55.2818, 120, 149.0, "Financial Centre"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-al-fahidi", "Al Fahidi Historical Neighbourhood", "Culture & Heritage",
                "Bur Dubai, Dubai, UAE", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Al_Bastakiya%2C_Dubai%2C_2016-10-18.jpg/800px-Al_Bastakiya%2C_Dubai%2C_2016-10-18.jpg",
                "Historic 19th-century wind-tower quarter housing contemporary art galleries, cultural cafes, and the Sheikh Mohammed Cultural Centre.",
                "https://dubaiculture.gov.ae", "Al Fahidi St, Bur Dubai",
                25.2635, 55.2974, 120, 0.0, "Old Dubai"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-arabian-tea-house", "Arabian Tea House Cafe", "Food",
                "Al Fahidi, Bur Dubai, Dubai, UAE", "4.7",
                null,
                "Charming heritage courtyard cafe serving authentic Emirati breakfasts, Karak chai, Machboos, and fresh mint lemonade.",
                "https://arabianteahouse.com", "Bastakiya Opposite Musalla Post Office, Al Fahidi",
                25.2638, 55.2970, 60, 75.0, "Old Dubai"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-creek-abra", "Dubai Creek Abra Ride", "Sightseeing & Culture",
                "Deira / Bur Dubai, Dubai, UAE", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Dubai_Creek_Abra_ride.jpg/800px-Dubai_Creek_Abra_ride.jpg",
                "Historic motorized wooden abra boat journey across Dubai Creek connecting the trading districts of Bur Dubai and Deira.",
                "https://www.visitdubai.com", "Dubai Creek Marine Transport Station, Bur Dubai",
                25.2650, 55.2950, 45, 2.0, "Old Dubai"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-souks", "Dubai Gold & Spice Souks", "Shopping & Culture",
                "Deira, Dubai, UAE", "4.5",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6e/Dubai_Spice_Souk_2015.jpg/800px-Dubai_Spice_Souk_2015.jpg",
                "Vibrant traditional Arabian bazaars overflowing with pure gold jewelry, saffron, frankincense, dried limes, and spices.",
                "https://www.visitdubai.com", "Al Ras, Deira",
                25.2683, 55.2995, 90, 0.0, "Old Dubai"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-al-seef", "Al Seef Heritage Waterfront", "Culture & Dining",
                "Bur Dubai, Dubai, UAE", "4.6",
                null,
                "Scenic pedestrian promenade on the banks of Dubai Creek combining traditional coral-stone architecture with modern dining.",
                "https://www.alseef.ae", "Al Seef St, Umm Hurair 1, Bur Dubai",
                25.2605, 55.3060, 90, 0.0, "Old Dubai"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-frame", "Dubai Frame", "Landmark",
                "Zabeel Park, Dubai, UAE", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Dubai_Frame_%2849925208622%29.jpg/800px-Dubai_Frame_%2849925208622%29.jpg",
                "150-meter golden observatory structure framing panoramic views of historic Old Dubai to the north and modern skyscrapers to the south.",
                "https://www.dubaiframe.ae", "Zabeel Park Jogging Track, Gate 4",
                25.2345, 55.3003, 90, 50.0, "Zabeel"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-palm-view", "The View at The Palm & Palm Jumeirah", "Sightseeing",
                "Palm Jumeirah, Dubai, UAE", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Palm_Jumeirah_aerial.jpg/800px-Palm_Jumeirah_aerial.jpg",
                "360-degree observation deck on level 52 of The Palm Tower offering grand vistas of the palm-shaped island archipelago.",
                "https://theviewpalm.ae", "The Palm Tower, Nakheel Mall, Palm Jumeirah",
                25.1166, 55.1389, 90, 100.0, "Palm Jumeirah"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-aquaventure", "Atlantis Aquaventure Waterpark", "Adventure",
                "Palm Jumeirah, Dubai, UAE", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/2/23/Aquaventure_Waterpark_Dubai.jpg/800px-Aquaventure_Waterpark_Dubai.jpg",
                "Award-winning mammoth waterpark featuring record-breaking waterslides, private beach access, and marine animal safaris.",
                "https://www.atlantis.com/dubai/atlantis-aquaventure", "Atlantis The Palm, Crescent Rd, Palm Jumeirah",
                25.1337, 55.1203, 240, 345.0, "Palm Jumeirah"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-marina-walk", "Dubai Marina Walk & Promenade", "Waterfront & Leisure",
                "Dubai Marina, Dubai, UAE", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Dubai_Marina_Walk.jpg/800px-Dubai_Marina_Walk.jpg",
                "Lively 7km palm-shaded marina pathway flanked by luxury mega-yachts, outdoor cafes, and illuminated residential towers.",
                "https://www.visitdubai.com", "Dubai Marina Walk, Dubai Marina",
                25.0784, 55.1415, 120, 0.0, "Dubai Marina"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-jbr-beach", "JBR Beach & The Walk", "Beaches",
                "Jumeirah Beach Residence, Dubai, UAE", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/JBR_Beach_Dubai.jpg/800px-JBR_Beach_Dubai.jpg",
                "Popular white-sand seaside beach offering watersports, camel rides, beach volleyball, and an open-air retail promenade.",
                "https://www.visitdubai.com", "The Walk, JBR, Dubai",
                25.0780, 55.1320, 120, 0.0, "Dubai Marina"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-madinat-jumeirah", "Souk Madinat Jumeirah", "Culture & Dining",
                "Umm Suqeim 3, Dubai, UAE", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/Madinat_Jumeirah_resort_Dubai.jpg/800px-Madinat_Jumeirah_resort_Dubai.jpg",
                "Arabian resort oasis with traditional winding bazaar alleyways, scenic abra canal waterways, and fine dining.",
                "https://www.jumeirah.com", "King Salman Bin Abdulaziz Al Saud St, Umm Suqeim 3",
                25.1330, 55.1852, 120, 0.0, "Jumeirah Coast"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-sunset-beach", "Sunset Beach & Burj Al Arab Viewpoint", "Beaches & Photography",
                "Umm Suqeim 2, Dubai, UAE", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Burj_Al_Arab_from_Sunset_Beach.jpg/800px-Burj_Al_Arab_from_Sunset_Beach.jpg",
                "Serene public beach offering the premier sunset vantage point of the iconic sail-shaped Burj Al Arab hotel.",
                "https://www.visitdubai.com", "22 Al Sagi St, Umm Suqeim 2",
                25.1412, 55.1855, 60, 0.0, "Jumeirah Coast"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-pierchic", "Pierchic Restaurant", "Food",
                "Madinat Jumeirah, Dubai, UAE", "4.7",
                null,
                "Multi-award-winning Italian coastal restaurant perched at the end of a private wooden pier above the turquoise Arabian Gulf.",
                "https://www.jumeirah.com/pierchic", "Jumeirah Al Qasr, Madinat Jumeirah",
                25.1360, 55.1870, 90, 250.0, "Jumeirah Coast"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-desert-safari", "Dubai Desert Conservation Reserve Safari", "Adventure",
                "Murqquab, Dubai, UAE", "4.8",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Dune_Bashing_Dubai.jpg/800px-Dune_Bashing_Dubai.jpg",
                "Thrilling 4x4 dune bashing expedition, sandboarding, camel riding, and sunset barbecue under starlit desert skies.",
                "https://www.visitdubai.com", "Dubai Desert Conservation Reserve, E66",
                24.8350, 55.6500, 240, 280.0, "Desert Excursion"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-miracle-garden", "Dubai Miracle Garden", "Nature & Park",
                "Al Barsha South 3, Dubai, UAE", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Dubai_Miracle_Garden_2016.jpg/800px-Dubai_Miracle_Garden_2016.jpg",
                "World's largest natural floral garden with over 150 million blossoming flowers arranged in arches, castles, and an Emirates A380.",
                "https://www.dubaimiraclegarden.com", "Street 3, Al Barsha South 3",
                25.0601, 55.2443, 120, 95.0, "Dubailand"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-global-village", "Global Village Dubai", "Culture & Entertainment",
                "Dubailand, Dubai, UAE", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Global_Village_Dubai_2017.jpg/800px-Global_Village_Dubai_2017.jpg",
                "Expansive open-air cultural theme park bringing together pavilions, cuisines, handicrafts, and concerts from 90+ nations.",
                "https://globalvillage.ae", "Sheikh Mohamed Bin Zayed Rd, Exit 37",
                25.0678, 55.3050, 180, 25.0, "Dubailand"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-kite-beach", "Kite Beach", "Beaches & Adventure",
                "Umm Suqeim 1, Dubai, UAE", "4.6",
                null,
                "Lively community beach known for kitesurfing, beach tennis courts, jogging track, and casual food trucks including SALT.",
                "https://www.kitebeach.ae", "Street 5a, Umm Suqeim 1",
                25.1610, 55.2070, 120, 0.0, "Jumeirah Coast"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-ravi-restaurant", "Ravi Restaurant", "Food",
                "Al Satwa, Dubai, UAE", "4.4",
                null,
                "Cult culinary institution beloved by locals and travelers for authentic Pakistani chicken karahi, mutton biryani, and hot naans.",
                "https://www.visitdubai.com", "Al Dhiyafa Rd, Near Satwa Roundabout",
                25.2312, 55.2754, 60, 45.0, "Old Dubai"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-bluewaters", "Bluewaters Island & Ain Dubai", "Sightseeing & Leisure",
                "Bluewaters Island, Dubai, UAE", "4.6",
                null,
                "Sophisticated artificial island destination featuring the colossal Ain Dubai wheel, coastal dining, and pedestrian bridge to JBR.",
                "https://www.bluewatersdubai.ae", "Bluewaters Island, Dubai",
                25.0798, 55.1225, 90, 0.0, "Dubai Marina"
        ));
        dubai.add(new PlaceItemDto(
                "dubai-etihad-museum", "Etihad Museum", "Museum & History",
                "Jumeirah 1, Dubai, UAE", "4.6",
                null,
                "Historic pavilion and underground museum marking Union House where the United Arab Emirates constitution was signed in 1971.",
                "https://etihadmuseum.dubaiculture.gov.ae", "1 Jumeirah St, Jumeirah 1",
                25.2405, 55.2672, 90, 25.0, "Jumeirah Coast"
        ));
        REGISTRY.put("dubai", dubai);

        // ═══════════════════════════════════════════════════════════════
        // HYDERABAD, INDIA
        // ═══════════════════════════════════════════════════════════════
        List<PlaceItemDto> hyd = new ArrayList<>();
        hyd.add(new PlaceItemDto(
                "hyd-charminar", "Charminar", "Landmark & Heritage",
                "Old City, Hyderabad, Telangana, India", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/71/Charminar_Hyderabad_1.jpg/800px-Charminar_Hyderabad_1.jpg",
                "Iconic 1591 CE Indo-Islamic monument with four grand arched minarets overlooking the historic heart of Hyderabad.",
                "https://telanganatourism.gov.in", "Charminar Rd, Char Kaman, Ghansi Bazaar",
                17.3616, 78.4747, 90, 25.0, "Old City Heritage"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-laad-bazaar", "Laad Bazaar & Choodi Bazaar", "Shopping & Culture",
                "Charminar, Hyderabad, Telangana, India", "4.5",
                null,
                "Centuries-old historic market famous for traditional stone-studded lacquer bangles, pearls, bidriware, and zari textiles.",
                "https://telanganatourism.gov.in", "Beside Charminar, Ghansi Bazaar",
                17.3610, 78.4735, 75, 0.0, "Old City Heritage"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-chowmahalla", "Chowmahalla Palace", "Palace & Heritage",
                "Khilwat, Hyderabad, Telangana, India", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Chowmahalla_Palace_Durbar_Hall.jpg/800px-Chowmahalla_Palace_Durbar_Hall.jpg",
                "Magnificent 18th-century palace of the Asaf Jahi dynasty featuring the opulent Khilwat Mubarak durbar hall and vintage Nizam cars.",
                "https://chowmahalla.in", "20-4-236, Motigalli, Khilwat",
                17.3578, 78.4717, 120, 100.0, "Old City Heritage"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-shadab", "Hotel Shadab", "Food",
                "Ghansi Bazaar, Hyderabad, Telangana, India", "4.5",
                null,
                "Legendary culinary gem near Charminar revered for authentic Hyderabadi mutton dum biryani, haleem, and paya soup.",
                "https://telanganatourism.gov.in", "21-1-140/144, Near Madina Circle, High Court Rd",
                17.3670, 78.4740, 75, 350.0, "Old City Heritage"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-salar-jung", "Salar Jung Museum", "Museum",
                "Darulshifa, Hyderabad, Telangana, India", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Salar_Jung_Museum_Hyderabad.jpg/800px-Salar_Jung_Museum_Hyderabad.jpg",
                "One of India's premier national museums housing prized global antiquities, the Veiled Rebecca statue, and the musical clock.",
                "https://salarjungmuseum.in", "Salar Jung Rd, Darulshifa",
                17.3713, 78.4804, 150, 50.0, "Museums & Arts"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-golconda", "Golconda Fort", "Fort & Heritage",
                "Golconda, Hyderabad, Telangana, India", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Golconda_Fort_Hyderabad.jpg/800px-Golconda_Fort_Hyderabad.jpg",
                "Awe-inspiring medieval fortress with legendary acoustic ingenuity, royal palace ruins, and the sound & light show.",
                "https://telanganatourism.gov.in", "Ibrahim Bagh, Golconda",
                17.3833, 78.4011, 180, 25.0, "Forts & Tombs"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-qutb-shahi", "Qutb Shahi Tombs", "Heritage & Architecture",
                "Ibrahim Bagh, Hyderabad, Telangana, India", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Qutb_Shahi_Tombs_Hyderabad.jpg/800px-Qutb_Shahi_Tombs_Hyderabad.jpg",
                "Graceful domed mausoleums set amidst landscaped gardens honoring the seven rulers of the Qutb Shahi dynasty.",
                "https://telanganatourism.gov.in", "Fort Rd, Near Golconda, Ibrahim Bagh",
                17.3916, 78.3970, 90, 40.0, "Forts & Tombs"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-hussain-sagar", "Hussain Sagar Lake & Buddha Statue", "Nature & Landmark",
                "Tank Bund, Hyderabad, Telangana, India", "4.5",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Buddha_Statue_in_Hussain_Sagar_Hyderabad.jpg/800px-Buddha_Statue_in_Hussain_Sagar_Hyderabad.jpg",
                "Historic 16th-century heart-shaped lake with motorboat rides to the world's tallest monolithic Buddha statue.",
                "https://telanganatourism.gov.in", "Tank Bund Rd, Secunderabad",
                17.4239, 78.4738, 90, 100.0, "Lakefront & Parks"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-birla-mandir", "Birla Mandir", "Culture & Viewpoint",
                "Naubat Pahad, Hyderabad, Telangana, India", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Birla_Mandir_Hyderabad.jpg/800px-Birla_Mandir_Hyderabad.jpg",
                "Pristine white Rajasthani marble temple perched atop Naubat Pahad hill providing panoramic views of Hyderabad and Hussain Sagar.",
                "https://telanganatourism.gov.in", "Hill Fort Rd, Ambedkar Colony, Khairatabad",
                17.4062, 78.4691, 60, 0.0, "Lakefront & Parks"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-ramoji", "Ramoji Film City", "Entertainment & Adventure",
                "Anaspur, Hyderabad, Telangana, India", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/Ramoji_Film_City_Hyderabad.jpg/800px-Ramoji_Film_City_Hyderabad.jpg",
                "Guinness World Record holding 2,000-acre cinematic wonderland with movie sets, live stunt shows, and adventure rides.",
                "https://ramojifilmcity.com", "Ramoji Film City Main Rd, Anaspur Village, Hayathnagar",
                17.2543, 78.6808, 300, 1350.0, "Film City Excursion"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-shilparamam", "Shilparamam Arts & Crafts Village", "Culture & Shopping",
                "HITEC City, Hyderabad, Telangana, India", "4.5",
                null,
                "Traditional arts and crafts village celebrating rural Indian art, handlooms, terracotta pottery, and open-air folk performances.",
                "https://shilparamam.in", "Hi-Tech City Main Rd, Madhapur",
                17.4526, 78.3789, 120, 60.0, "Modern Tech & Waterfront"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-durgam-cheruvu", "Durgam Cheruvu Cable Bridge & Lakefront", "Waterfront & Leisure",
                "HITEC City / Jubilee Hills, Hyderabad, Telangana, India", "4.6",
                null,
                "Scenic illuminated cable-stayed bridge and freshwater lake with pedal boating, waterfront boardwalks, and evening musical fountains.",
                "https://telanganatourism.gov.in", "CBI Colony, Jubilee Hills",
                17.4340, 78.3880, 90, 0.0, "Modern Tech & Waterfront"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-paradise", "Paradise Food Court", "Food",
                "Secunderabad, Telangana, India", "4.4",
                null,
                "The historic flagship home of authentic Hyderabadi biryani serving aromatic basmati rice layered with tender spiced meat since 1953.",
                "https://paradisefoodcourt.in", "SD Road, Sappu Bagh Apartment, Jogani, Secunderabad",
                17.4416, 78.4983, 75, 400.0, "Secunderabad Trail"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-kbr-park", "Kasu Brahmananda Reddy (KBR) National Park", "Nature & Park",
                "Jubilee Hills, Hyderabad, Telangana, India", "4.5",
                null,
                "390-acre lush sanctuary in the heart of Jubilee Hills featuring tranquil walking trails, Chiran Palace heritage, and diverse bird species.",
                "https://telanganatourism.gov.in", "Road No 2, Jubilee Hills",
                17.4230, 78.4170, 90, 40.0, "Modern Tech & Waterfront"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-nehru-zoo", "Nehru Zoological Park", "Nature & Wildlife",
                "Bahadurpura, Hyderabad, Telangana, India", "4.5",
                null,
                "Sprawling 380-acre botanical and wildlife park featuring Asiatic lion safari, toy train, nocturnal house, and butterfly garden.",
                "https://nehruzoopark.telangana.gov.in", "Zoo Park Main Rd, Bahadurpura",
                17.3508, 78.4517, 150, 70.0, "Nature & Wildlife"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-chutneys", "Chutneys Banjara Hills", "Food",
                "Banjara Hills, Hyderabad, Telangana, India", "4.5",
                null,
                "Beloved South Indian restaurant celebrated for steaming Babai Idli dipped in pure ghee, crispy dosas, and six varieties of signature chutneys.",
                "https://telanganatourism.gov.in", "Road No 3, Green Valley, Banjara Hills",
                17.4180, 78.4480, 60, 300.0, "Lakefront & Parks"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-nizam-museum", "Nizam's Museum", "Museum",
                "Purani Haveli, Hyderabad, Telangana, India", "4.4",
                null,
                "Museum housed in the Purani Haveli palace displaying the 1937 silver jubilee gifts of Mir Osman Ali Khan and the 176-foot wardrobe.",
                "https://telanganatourism.gov.in", "Purani Haveli, Pathar Gatti",
                17.3660, 78.4840, 75, 80.0, "Museums & Arts"
        ));
        hyd.add(new PlaceItemDto(
                "hyd-taramati", "Taramati Baradari", "Heritage & Culture",
                "Ibrahim Bagh, Hyderabad, Telangana, India", "4.4",
                null,
                "Historical Persian-styled 12-door music pavilion constructed by Abdullah Qutb Shah with celebrated natural acoustic resonance.",
                "https://telanganatourism.gov.in", "Near Golconda Fort, PBEL City Road, Ibrahim Bagh",
                17.3820, 78.3810, 60, 20.0, "Forts & Tombs"
        ));
        REGISTRY.put("hyderabad", hyd);

        // ═══════════════════════════════════════════════════════════════
        // PARIS, FRANCE
        // ═══════════════════════════════════════════════════════════════
        List<PlaceItemDto> paris = new ArrayList<>();
        paris.add(new PlaceItemDto(
                "paris-eiffel", "Eiffel Tower", "Landmark",
                "Champ de Mars, Paris, France", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/8/85/Tour_Eiffel_Wikimedia_Commons_%28cropped%29.jpg/800px-Tour_Eiffel_Wikimedia_Commons_%28cropped%29.jpg",
                "Iconic 330-meter wrought-iron lattice monument erected for the 1889 World's Fair offering panoramic summit views of Paris.",
                "https://www.toureiffel.paris", "Champ de Mars, 5 Av. Anatole France, 75007 Paris",
                48.8584, 2.2945, 120, 29.0, "Iconic Paris & Seine"
        ));
        paris.add(new PlaceItemDto(
                "paris-louvre", "Louvre Museum", "Museum",
                "1st Arrondissement, Paris, France", "4.8",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Louvre_Museum_Wikimedia_Commons.jpg/800px-Louvre_Museum_Wikimedia_Commons.jpg",
                "The world's largest art museum housed in a former royal fortress, home to Leonardo da Vinci's Mona Lisa and the Winged Victory.",
                "https://www.louvre.fr", "Rue de Rivoli, 75001 Paris",
                48.8606, 2.3376, 180, 22.0, "Historic Center & Louvre"
        ));
        paris.add(new PlaceItemDto(
                "paris-orsay", "Musée d'Orsay", "Museum",
                "7th Arrondissement, Paris, France", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7c/Mus%C3%A9e_d%27Orsay%2C_Paris_14_May_2014.jpg/800px-Mus%C3%A9e_d%27Orsay%2C_Paris_14_May_2014.jpg",
                "Magnificent former railway station showcasing masterpieces of Impressionism and Post-Impressionism by Monet, Van Gogh, and Renoir.",
                "https://www.musee-orsay.fr", "1 Rue de la Légion d'Honneur, 75007 Paris",
                48.8599, 2.3265, 150, 16.0, "Historic Center & Louvre"
        ));
        paris.add(new PlaceItemDto(
                "paris-notre-dame", "Cathédrale Notre-Dame de Paris", "Heritage & Landmark",
                "Île de la Cité, Paris, France", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/af/Notre-Dame_de_Paris_2013-07-24.jpg/800px-Notre-Dame_de_Paris_2013-07-24.jpg",
                "Masterpiece of French Gothic architecture on the Île de la Cité renowned for its flying buttresses, gargoyles, and rose windows.",
                "https://www.notredamedeparis.fr", "6 Parvis Notre-Dame - Pl. Jean-Paul II, 75004 Paris",
                48.8530, 2.3499, 90, 0.0, "Île de la Cité & Latin Quarter"
        ));
        paris.add(new PlaceItemDto(
                "paris-sainte-chapelle", "Sainte-Chapelle", "Heritage",
                "Île de la Cité, Paris, France", "4.8",
                null,
                "13th-century royal Gothic chapel celebrated worldwide for its 15 towering stained glass windows depicting over 1,100 biblical scenes.",
                "https://www.sainte-chapelle.fr", "10 Bd du Palais, 75001 Paris",
                48.8554, 2.3450, 60, 13.0, "Île de la Cité & Latin Quarter"
        ));
        paris.add(new PlaceItemDto(
                "paris-arc-triomphe", "Arc de Triomphe & Champs-Élysées", "Landmark & Shopping",
                "8th Arrondissement, Paris, France", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c4/Arc_Triomphe.jpg/800px-Arc_Triomphe.jpg",
                "Triumphal arch standing at the western end of the Champs-Élysées with a rooftop viewing platform honoring the soldiers of France.",
                "https://www.paris-arc-de-triomphe.fr", "Pl. Charles de Gaulle, 75008 Paris",
                48.8738, 2.2950, 90, 16.0, "Iconic Paris & Seine"
        ));
        paris.add(new PlaceItemDto(
                "paris-sacre-coeur", "Sacré-Cœur Basilica & Montmartre", "Culture & Viewpoint",
                "Montmartre, Paris, France", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d2/Basilique_du_Sacr%C3%A9-C%C5%93ur_de_Montmartre_at_night_2015.jpg/800px-Basilique_du_Sacr%C3%A9-C%C5%93ur_de_Montmartre_at_night_2015.jpg",
                "Dazzling white travertine basilica crowning the summit of bohemian Montmartre overlooking the Paris skyline.",
                "https://www.sacre-coeur-montmartre.com", "35 Rue du Chevalier de la Barre, 75018 Paris",
                48.8867, 2.3431, 120, 0.0, "Montmartre & Artists"
        ));
        paris.add(new PlaceItemDto(
                "paris-pompidou", "Centre Pompidou", "Museum & Art",
                "4th Arrondissement, Paris, France", "4.5",
                null,
                "Bold high-tech architectural landmark housing Europe's largest museum of modern and contemporary art and rooftop vistas.",
                "https://www.centrepompidou.fr", "Place Georges-Pompidou, 75004 Paris",
                48.8606, 2.3522, 120, 15.0, "Le Marais & Modern Arts"
        ));
        paris.add(new PlaceItemDto(
                "paris-jardin-luxembourg", "Jardin du Luxembourg", "Nature & Park",
                "6th Arrondissement, Paris, France", "4.7",
                null,
                "Tranquil 25-hectare formal gardens commissioned by Marie de' Medici featuring the Medici Fountain, model sailboats, and tree-lined allées.",
                "https://www.senat.fr/visite/jardin/", "Rue de Médicis - Rue de Vaugirard, 75006 Paris",
                48.8462, 2.3371, 90, 0.0, "Latin Quarter & Saint-Germain"
        ));
        paris.add(new PlaceItemDto(
                "paris-seine-cruise", "Seine River Cruise via Bateaux Mouches", "Sightseeing",
                "Pont de l'Alma, Paris, France", "4.6",
                null,
                "Glide past UNESCO-listed riverbanks, the Pont Neuf, Louvre, and illuminated bridges on an open-air riverboat cruise.",
                "https://www.bateaux-mouches.fr", "Port de la Conférence, 75008 Paris",
                48.8637, 2.3020, 75, 17.0, "Iconic Paris & Seine"
        ));
        paris.add(new PlaceItemDto(
                "paris-marais", "Le Marais Historic District", "Culture & Dining",
                "3rd & 4th Arrondissements, Paris, France", "4.7",
                null,
                "Chic historic neighborhood of cobblestone streets, private aristocratic mansions (hôtels particuliers), art galleries, and falafel stalls.",
                "https://www.parisjetaime.com", "Rue des Rosiers / Place des Vosges, 75004 Paris",
                48.8575, 2.3620, 120, 0.0, "Le Marais & Modern Arts"
        ));
        paris.add(new PlaceItemDto(
                "paris-marche-enfants-rouges", "Marché des Enfants Rouges", "Food",
                "3rd Arrondissement, Paris, France", "4.6",
                null,
                "Paris's oldest covered food market established in 1615 offering vibrant stalls of Moroccan tagines, Lebanese meze, and French cheeses.",
                "https://www.paris.fr", "39 Rue de Bretagne, 75003 Paris",
                48.8630, 2.3630, 75, 20.0, "Le Marais & Modern Arts"
        ));
        paris.add(new PlaceItemDto(
                "paris-bouillon-chartier", "Bouillon Chartier", "Food",
                "Grands Boulevards, Paris, France", "4.4",
                null,
                "Legendary Belle Époque brasserie founded in 1896 serving authentic traditional French dishes with waiters noting orders on the tablecloth.",
                "https://www.bouillon-chartier.com", "7 Rue du Faubourg Montmartre, 75009 Paris",
                48.8720, 2.3430, 75, 20.0, "Montmartre & Artists"
        ));
        paris.add(new PlaceItemDto(
                "paris-opera-garnier", "Palais Garnier Opera House", "Heritage & Culture",
                "9th Arrondissement, Paris, France", "4.8",
                null,
                "Opulent neo-Baroque opera house renowned for its grand marble staircase, gilded auditorium, and Chagall ceiling painting.",
                "https://www.operadeparis.fr", "Pl. de l'Opéra, 75009 Paris",
                48.8719, 2.3316, 90, 15.0, "Montmartre & Artists"
        ));
        paris.add(new PlaceItemDto(
                "paris-galeries-lafayette", "Galeries Lafayette Haussmann", "Shopping",
                "Boulevard Haussmann, Paris, France", "4.5",
                null,
                "Flagship Parisian grand department store featuring an iconic Neo-Byzantine stained glass dome and a free panoramic rooftop terrace.",
                "https://www.galerieslafayette.com", "40 Bd Haussmann, 75009 Paris",
                48.8735, 2.3320, 120, 0.0, "Montmartre & Artists"
        ));
        paris.add(new PlaceItemDto(
                "paris-cafe-de-flore", "Café de Flore", "Food",
                "Saint-Germain-des-Prés, Paris, France", "4.4",
                null,
                "Historic literary cafe in Saint-Germain famous as the intellectual salon of Jean-Paul Sartre, Simone de Beauvoir, and Picasso.",
                "https://cafedeflore.fr", "172 Bd Saint-Germain, 75006 Paris",
                48.8540, 2.3330, 60, 25.0, "Latin Quarter & Saint-Germain"
        ));
        paris.add(new PlaceItemDto(
                "paris-pantheon", "The Panthéon", "Heritage",
                "Latin Quarter, Paris, France", "4.6",
                null,
                "Neoclassical national mausoleum in the Latin Quarter honoring French national luminaries including Voltaire, Rousseau, and Marie Curie.",
                "https://www.paris-pantheon.fr", "Pl. du Panthéon, 75005 Paris",
                48.8462, 2.3464, 90, 13.0, "Latin Quarter & Saint-Germain"
        ));
        paris.add(new PlaceItemDto(
                "paris-versailles", "Palace of Versailles", "Palace & Heritage",
                "Versailles, France", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/Chateau_Versailles_Galerie_des_Glaces.jpg/800px-Chateau_Versailles_Galerie_des_Glaces.jpg",
                "Sun King Louis XIV's breathtaking royal residence featuring the Hall of Mirrors, King's Grand Apartments, and fountain gardens.",
                "https://www.chateauversailles.fr", "Place d'Armes, 78000 Versailles",
                48.8049, 2.1204, 240, 21.0, "Versailles Excursion"
        ));
        REGISTRY.put("paris", paris);

        // ═══════════════════════════════════════════════════════════════
        // TOKYO, JAPAN
        // ═══════════════════════════════════════════════════════════════
        List<PlaceItemDto> tokyo = new ArrayList<>();
        tokyo.add(new PlaceItemDto(
                "tokyo-sensoji", "Senso-ji Temple & Nakamise-dori", "Culture & Heritage",
                "Asakusa, Taito City, Tokyo, Japan", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Sens%C5%8D-ji_temple%2C_Tokyo.jpg/800px-Sens%C5%8D-ji_temple%2C_Tokyo.jpg",
                "Tokyo's oldest and most revered Buddhist temple entered through the iconic Kaminarimon thunder gate and historic shopping street.",
                "https://www.senso-ji.jp", "2 Chome-3-1 Asakusa, Taito City",
                35.7148, 139.7967, 120, 0.0, "Traditional Asakusa"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-skytree", "Tokyo Skytree", "Landmark",
                "Sumida City, Tokyo, Japan", "4.6",
                null,
                "World's tallest free-standing broadcasting tower at 634m with observation decks offering views stretching to Mount Fuji.",
                "https://www.tokyo-skytree.jp", "1 Chome-1-2 Oshiage, Sumida City",
                35.7100, 139.8107, 90, 2100.0, "Traditional Asakusa"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-shibuya-crossing", "Shibuya Crossing & Hachiko Statue", "Landmark & Vibrant City",
                "Shibuya City, Tokyo, Japan", "4.6",
                null,
                "The world's busiest pedestrian scramble crossing surrounded by neon billboards, next to the faithful Hachiko dog statue.",
                "https://www.gotokyo.org", "2 Chome-2-1 Dogenzaka, Shibuya City",
                35.6595, 139.7005, 60, 0.0, "Shibuya & Harajuku"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-meiji-shrine", "Meiji Jingu Shrine", "Culture & Nature",
                "Shibuya City, Tokyo, Japan", "4.7",
                null,
                "Serene Shinto shrine dedicated to Emperor Meiji surrounded by an evergreen forest of over 100,000 trees.",
                "https://www.meijijingu.or.jp", "1-1 Yoyogikamizonacho, Shibuya City",
                35.6764, 139.6993, 90, 0.0, "Shibuya & Harajuku"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-shinjuku-gyoen", "Shinjuku Gyoen National Garden", "Nature & Park",
                "Shinjuku City, Tokyo, Japan", "4.7",
                null,
                "Sprawling national park blending traditional Japanese landscape gardens with English classical and French formal gardens.",
                "https://www.env.go.jp/garden/shinjukugyoen", "11 Naitomachi, Shinjuku City",
                35.6852, 139.7101, 120, 500.0, "Shinjuku Metropolis"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-teamlab", "teamLab Planets TOKYO", "Art & Entertainment",
                "Toyosu, Koto City, Tokyo, Japan", "4.8",
                null,
                "Immersive body-interactive digital art museum where visitors walk barefoot through water and infinite crystal universes.",
                "https://planets.teamlab.art", "6 Chome-1-16 Toyosu, Koto City",
                35.6518, 139.7915, 120, 3800.0, "Tokyo Bay & Waterfront"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-tsukiji", "Tsukiji Outer Market", "Food",
                "Chuo City, Tokyo, Japan", "4.5",
                null,
                "Vibrant culinary market lined with narrow stalls offering freshest sushi, sashimi, grilled wagyu skewers, and tamagoyaki omelets.",
                "https://www.tsukiji.or.jp", "4 Chome-16-2 Tsukiji, Chuo City",
                35.6655, 139.7706, 90, 1500.0, "Ginza & Central Tokyo"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-akihabara", "Akihabara Electric Town", "Shopping & Culture",
                "Chiyoda City, Tokyo, Japan", "4.4",
                null,
                "Global capital of anime, manga, retro video games, maid cafes, and multi-story consumer electronics stores.",
                "https://www.gotokyo.org", "Sotokanda, Chiyoda City",
                35.6997, 139.7714, 120, 0.0, "Central Tokyo Heritage"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-tower", "Tokyo Tower & Zojo-ji Temple", "Landmark",
                "Minato City, Tokyo, Japan", "4.6",
                null,
                "Communications tower inspired by the Eiffel Tower painted in white and international orange, framed beside historic Zojo-ji temple.",
                "https://www.tokyotower.co.jp", "4 Chome-2-8 Shibakoen, Minato City",
                35.6586, 139.7454, 90, 1200.0, "Minato & Roppongi"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-omoide-yokocho", "Omoide Yokocho (Memory Lane)", "Food",
                "Shinjuku City, Tokyo, Japan", "4.5",
                null,
                "Nostalgic post-war alleyway packed with tiny smoky yakitori counters, ramen shops, and cold draft beer.",
                "https://shinjuku-omoide.com", "1 Chome-2 Nishishinjuku, Shinjuku City",
                35.6931, 139.6997, 75, 2000.0, "Shinjuku Metropolis"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-ueno-park", "Ueno Park & Tokyo National Museum", "Museum & Nature",
                "Taito City, Tokyo, Japan", "4.6",
                null,
                "Vast public park famous for cherry blossoms, Shinobazu Pond, and Japan's oldest and largest museum of Asian antiquities.",
                "https://www.tnm.jp", "Uenokoen, Taito City",
                35.7188, 139.7765, 150, 1000.0, "Traditional Asakusa"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-ginza", "Ginza Luxury District", "Shopping",
                "Chuo City, Tokyo, Japan", "4.5",
                null,
                "Tokyo's premier upscale shopping, dining, and gallery district featuring flagship boutiques and Kabuki-za theater.",
                "https://www.ginza.jp", "Ginza, Chuo City",
                35.6719, 139.7649, 120, 0.0, "Ginza & Central Tokyo"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-imperial-palace", "Imperial Palace East Gardens", "Heritage & Nature",
                "Chiyoda City, Tokyo, Japan", "4.6",
                null,
                "Serene public gardens situated on the former grounds of Edo Castle featuring moat walls, guardhouses, and Ninomaru Japanese garden.",
                "https://www.kunaicho.go.jp", "1-1 Chiyoda, Chiyoda City",
                35.6852, 139.7528, 90, 0.0, "Central Tokyo Heritage"
        ));
        tokyo.add(new PlaceItemDto(
                "tokyo-odaiba", "Odaiba Seaside Park & Rainbow Bridge", "Waterfront & Leisure",
                "Minato City, Tokyo, Japan", "4.6",
                null,
                "Futuristic man-made island on Tokyo Bay featuring the life-size Unicorn Gundam statue, waterfront promenades, and bridge views.",
                "https://www.gotokyo.org", "1 Chome Daiba, Minato City",
                35.6298, 139.7764, 120, 0.0, "Tokyo Bay & Waterfront"
        ));
        REGISTRY.put("tokyo", tokyo);

        // ═══════════════════════════════════════════════════════════════
        // GOA, INDIA
        // ═══════════════════════════════════════════════════════════════
        List<PlaceItemDto> goa = new ArrayList<>();
        goa.add(new PlaceItemDto(
                "goa-fort-aguada", "Fort Aguada & Lighthouse", "Fort & Heritage",
                "Candolim, North Goa, India", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/Fort_Aguada_Lighthouse.jpg/800px-Fort_Aguada_Lighthouse.jpg",
                "Well-preserved 17th-century Portuguese fortress and lighthouse guarding the mouth of the Mandovi River with Arabian Sea vistas.",
                "https://goatourism.gov.in", "Aguada Fort Rd, Candolim, Goa 403515",
                15.4920, 73.7737, 90, 50.0, "North Goa Coast & Forts"
        ));
        goa.add(new PlaceItemDto(
                "goa-basilica", "Basilica of Bom Jesus", "Heritage & Religious Site",
                "Old Goa, Goa, India", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Basilica_of_Bom_Jesus_in_Goa.jpg/800px-Basilica_of_Bom_Jesus_in_Goa.jpg",
                "UNESCO World Heritage Baroque landmark enshrining the mortal sacred relics of St. Francis Xavier.",
                "https://goatourism.gov.in", "Old Goa Rd, Bainguinim, Goa 403402",
                15.5008, 73.9116, 75, 0.0, "Old Goa World Heritage"
        ));
        goa.add(new PlaceItemDto(
                "goa-se-cathedral", "Se Cathedral", "Heritage & Religious Site",
                "Old Goa, Goa, India", "4.6",
                null,
                "One of the largest churches in Asia dedicated to St. Catherine, known for its Tuscan exterior and the renowned Golden Bell.",
                "https://goatourism.gov.in", "Velha Goa, Goa 403402",
                15.5034, 73.9126, 60, 0.0, "Old Goa World Heritage"
        ));
        goa.add(new PlaceItemDto(
                "goa-fontainhas", "Fontainhas Latin Quarter", "Culture & Heritage",
                "Panaji, Goa, India", "4.6",
                null,
                "Picturesque heritage neighborhood in Panaji characterized by vibrant colonial Portuguese villas, tiled roofs, and art galleries.",
                "https://goatourism.gov.in", "Altinho, Panaji, Goa 403001",
                15.4989, 73.8310, 90, 0.0, "Panaji & Riverfront"
        ));
        goa.add(new PlaceItemDto(
                "goa-baga-beach", "Baga Beach", "Beaches",
                "Baga, North Goa, India", "4.4",
                null,
                "Lively beach strip famous for parasailing, jet skiing, beach shacks, and vibrant nightlife along Tito's Lane.",
                "https://goatourism.gov.in", "Baga Beach, Goa 403516",
                15.5553, 73.7516, 120, 0.0, "North Goa Coast & Forts"
        ));
        goa.add(new PlaceItemDto(
                "goa-anjuna-market", "Anjuna Beach & Flea Market", "Beaches & Shopping",
                "Anjuna, North Goa, India", "4.5",
                null,
                "Red-rock coastal beach celebrated for its bohemian Wednesday flea market, trance music heritage, and sunset shacks.",
                "https://goatourism.gov.in", "Anjuna, Goa 403509",
                15.5733, 73.7410, 120, 0.0, "North Goa Coast & Forts"
        ));
        goa.add(new PlaceItemDto(
                "goa-chapora-fort", "Chapora Fort & Vagator Beach", "Fort & Viewpoint",
                "Vagator, North Goa, India", "4.5",
                null,
                "Historic hilltop laterite fort made iconic by the movie Dil Chahta Hai, overlooking Vagator Beach and the Chapora River.",
                "https://goatourism.gov.in", "Chapora, Vagator, Goa 403509",
                15.6058, 73.7360, 90, 0.0, "North Goa Coast & Forts"
        ));
        goa.add(new PlaceItemDto(
                "goa-dudhsagar", "Dudhsagar Waterfalls", "Nature & Adventure",
                "Mollem National Park, Sonaulim, Goa, India", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Dudhsagar_Falls_Goa.jpg/800px-Dudhsagar_Falls_Goa.jpg",
                "Spectacular four-tiered 310m milky white waterfall surrounded by Western Ghats jungle reached by open 4x4 jeep safari.",
                "https://goatourism.gov.in", "Sonaulim, Goa 403410",
                15.3144, 74.3143, 240, 600.0, "Western Ghats Adventure"
        ));
        goa.add(new PlaceItemDto(
                "goa-palolem", "Palolem Beach", "Beaches & Relaxation",
                "Canacona, South Goa, India", "4.7",
                null,
                "Idyllic crescent-shaped white sand beach in South Goa fringed by coconut palms, calm swimming waters, and dolphin boat trips.",
                "https://goatourism.gov.in", "Canacona, South Goa 403702",
                15.0100, 74.0232, 180, 0.0, "South Goa Serenity"
        ));
        goa.add(new PlaceItemDto(
                "goa-spice-plantation", "Sahakari Spice Farm", "Nature & Food",
                "Ponda, Goa, India", "4.5",
                null,
                "Aromatic tropical spice plantation tour with traditional Goan buffet lunch served on banana leaves and betelnut tree demonstrations.",
                "https://goatourism.gov.in", "Ponda-Belgaum Highway, Curti, Ponda, Goa 403401",
                15.4120, 74.0270, 150, 500.0, "Central Goa Heritage"
        ));
        goa.add(new PlaceItemDto(
                "goa-fishermans-wharf", "The Fisherman's Wharf", "Food",
                "Cavelossim / Panaji, Goa, India", "4.6",
                null,
                "Acclaimed riverside dining serving authentic Goan prawn balchão, Kingfish rava fry, pork vindaloo, and bebinca dessert.",
                "https://thefishermanswharf.in", "At The Riverside, Mobor, Cavelossim, Goa 403731",
                15.1720, 73.9480, 90, 800.0, "South Goa Serenity"
        ));
        goa.add(new PlaceItemDto(
                "goa-gunpowder", "Gunpowder Restaurant", "Food",
                "Assagao, Goa, India", "4.6",
                null,
                "Celebrated rustic open-air garden restaurant in an old Portuguese home serving fiery Peninsular South Indian and coastal fare.",
                "https://goatourism.gov.in", "4/6, Saunto Vaddo, Assagao, Goa 403507",
                15.5900, 73.7800, 75, 700.0, "North Goa Coast & Forts"
        ));
        goa.add(new PlaceItemDto(
                "goa-mandovi-cruise", "Mandovi River Sunset Cruise", "Sightseeing",
                "Panaji, Goa, India", "4.4",
                null,
                "One-hour evening catamaran river cruise along the Mandovi River featuring live Goan folk dance performances and music.",
                "https://goatourism.gov.in", "Santa Monica Jetty, Panaji, Goa 403001",
                15.4980, 73.8290, 75, 500.0, "Panaji & Riverfront"
        ));
        REGISTRY.put("goa", goa);

        // ═══════════════════════════════════════════════════════════════
        // JAIPUR, INDIA
        // ═══════════════════════════════════════════════════════════════
        List<PlaceItemDto> jaipur = new ArrayList<>();
        jaipur.add(new PlaceItemDto(
                "jaipur-amber-fort", "Amber Fort & Palace", "Fort & Heritage",
                "Amer, Jaipur, Rajasthan, India", "4.7",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Amber_Fort_Jaipur.jpg/800px-Amber_Fort_Jaipur.jpg",
                "UNESCO World Heritage hill fortress built of yellow and pink sandstone with the legendary Sheesh Mahal (Mirror Palace).",
                "https://www.tourism.rajasthan.gov.in", "Devisinghpura, Amer, Jaipur 302001",
                26.9855, 75.8513, 180, 100.0, "Amer & Royal Forts"
        ));
        jaipur.add(new PlaceItemDto(
                "jaipur-hawa-mahal", "Hawa Mahal (Palace of Winds)", "Landmark & Heritage",
                "Badi Choupad, Jaipur, Rajasthan, India", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Hawa_Mahal_2011.jpg/800px-Hawa_Mahal_2011.jpg",
                "Iconic five-story red and pink sandstone honeycomb facade with 953 ornate latticed jharokhas built for royal women in 1799.",
                "https://www.tourism.rajasthan.gov.in", "Hawa Mahal Rd, Badi Choupad, J.D.A. Market, Jaipur 302002",
                26.9239, 75.8267, 60, 50.0, "Pink City Core"
        ));
        jaipur.add(new PlaceItemDto(
                "jaipur-city-palace", "City Palace of Jaipur", "Palace & Heritage",
                "Old City, Jaipur, Rajasthan, India", "4.6",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/City_Palace_Jaipur_Courtyard.jpg/800px-City_Palace_Jaipur_Courtyard.jpg",
                "Resplendent royal residence blending Rajput and Mughal architecture, housing the Maharaja Sawai Man Singh II Museum.",
                "https://royaljaipur.in", "Tulsi Marg, Gangori Bazaar, J.D.A. Market, Jaipur 302002",
                26.9258, 75.8236, 120, 200.0, "Pink City Core"
        ));
        jaipur.add(new PlaceItemDto(
                "jaipur-jantar-mantar", "Jantar Mantar Astronomical Observatory", "Heritage & Science",
                "Old City, Jaipur, Rajasthan, India", "4.6",
                null,
                "UNESCO World Heritage 18th-century stone astronomical observation complex featuring the world's largest stone sundial.",
                "https://www.tourism.rajasthan.gov.in", "Gangori Bazaar, J.D.A. Market, Pink City, Jaipur 302002",
                26.9248, 75.8246, 90, 50.0, "Pink City Core"
        ));
        jaipur.add(new PlaceItemDto(
                "jaipur-nahargarh", "Nahargarh Fort & Sunset Viewpoint", "Fort & Viewpoint",
                "Aravalli Hills, Jaipur, Rajasthan, India", "4.7",
                null,
                "Rugged fort on the ridge of the Aravalli hills overlooking the Pink City, celebrated for its Padao open-air sunset cafe.",
                "https://www.tourism.rajasthan.gov.in", "Krishna Nagar, Brahampuri, Jaipur 302002",
                26.9374, 75.8157, 120, 50.0, "Amer & Royal Forts"
        ));
        jaipur.add(new PlaceItemDto(
                "jaipur-jal-mahal", "Jal Mahal (Water Palace)", "Sightseeing",
                "Man Sagar Lake, Jaipur, Rajasthan, India", "4.5",
                null,
                "Serene 18th-century palace floating in the middle of Man Sagar Lake against the backdrop of the Aravalli hills.",
                "https://www.tourism.rajasthan.gov.in", "Amer Rd, Jal Mahal, Amer, Jaipur 302002",
                26.9656, 75.8456, 45, 0.0, "Amer & Royal Forts"
        ));
        jaipur.add(new PlaceItemDto(
                "jaipur-johari-bazaar", "Johari Bazaar & Bapu Bazaar", "Shopping & Culture",
                "Pink City, Jaipur, Rajasthan, India", "4.5",
                null,
                "Vibrant traditional bazaars famous for precious Kundan gemstone jewelry, Jaipuri quilts, mojari leather shoes, and bandhani sarees.",
                "https://www.tourism.rajasthan.gov.in", "Johari Bazar Rd, Pink City, Jaipur 302003",
                26.9200, 75.8250, 120, 0.0, "Pink City Core"
        ));
        jaipur.add(new PlaceItemDto(
                "jaipur-albert-hall", "Albert Hall Museum", "Museum",
                "Ram Niwas Garden, Jaipur, Rajasthan, India", "4.6",
                null,
                "Rajasthan's oldest museum housed in an Indo-Saracenic building displaying miniature paintings, Persian carpets, and an Egyptian mummy.",
                "https://www.tourism.rajasthan.gov.in", "Museum Rd, Ram Niwas Garden, Kailash Puri, Jaipur 302004",
                26.9116, 75.8195, 90, 40.0, "Southern Heritage & Gardens"
        ));
        jaipur.add(new PlaceItemDto(
                "jaipur-chokhi-dhani", "Chokhi Dhani Ethnic Village Resort", "Culture & Food",
                "Tonk Road, Jaipur, Rajasthan, India", "4.5",
                null,
                "Cultural heritage village resort with camel rides, fire dancers, puppet shows, and a lavish traditional Rajasthani thali feast.",
                "https://chokhidhani.com", "12 Miles Tonk Road, Goner Mod Flyover, Jaipur 303905",
                26.7660, 75.8340, 180, 900.0, "Cultural Excursion"
        ));
        jaipur.add(new PlaceItemDto(
                "jaipur-lmb", "Laxmi Mishthan Bhandar (LMB)", "Food",
                "Johari Bazaar, Jaipur, Rajasthan, India", "4.4",
                null,
                "Legendary sweet shop and restaurant established in 1727 famous for Ghewar, Pyaaz Kachori, and authentic Rajasthani Dal Baati Churma.",
                "https://hotellmb.com", "98-99, Johari Bazar Rd, Bapu Bazar, Pink City, Jaipur 302003",
                26.9205, 75.8260, 60, 400.0, "Pink City Core"
        ));
        jaipur.add(new PlaceItemDto(
                "jaipur-patrika-gate", "Patrika Gate & Jawahar Circle", "Landmark & Photography",
                "Malviya Nagar, Jaipur, Rajasthan, India", "4.6",
                null,
                "Breathtaking pastel-painted monumental gate decorated with hand-painted murals depicting architectural heritage of Rajasthan.",
                "https://www.tourism.rajasthan.gov.in", "Jawahar Circle, Malviya Nagar, Jaipur 302017",
                26.8288, 75.8058, 60, 0.0, "Southern Heritage & Gardens"
        ));
        REGISTRY.put("jaipur", jaipur);

        // ═══════════════════════════════════════════════════════════════
        // OOTY, INDIA
        // ═══════════════════════════════════════════════════════════════
        List<PlaceItemDto> ooty = new ArrayList<>();
        ooty.add(new PlaceItemDto(
                "ooty-toy-train", "Nilgiri Mountain Railway (Toy Train)", "Sightseeing & Heritage",
                "Ooty Railway Station, Tamil Nadu, India", "4.7",
                null,
                "UNESCO World Heritage steam locomotive railway traversing 16 tunnels and 250 bridges across misty blue Nilgiri tea hills.",
                "https://www.tamilnadutourism.tn.gov.in", "Railway Station Road, Ooty 643001",
                11.4064, 76.6953, 150, 205.0, "Central Ooty & Heritage"
        ));
        ooty.add(new PlaceItemDto(
                "ooty-botanical-garden", "Government Botanical Gardens", "Nature & Park",
                "Vannarapettai, Ooty, Tamil Nadu, India", "4.6",
                null,
                "55-acre terraced garden established in 1848 boasting over 1,000 species of exotic flora and a 20-million-year-old fossilized tree trunk.",
                "https://www.tamilnadutourism.tn.gov.in", "Vannarapettai, Ooty 643002",
                11.4178, 76.7112, 120, 40.0, "Central Ooty & Heritage"
        ));
        ooty.add(new PlaceItemDto(
                "ooty-lake", "Ooty Lake & Boathouse", "Waterfront & Leisure",
                "Westmere, Ooty, Tamil Nadu, India", "4.4",
                null,
                "Artificial lake created in 1824 by John Sullivan offering row boats, paddle boats, motor boats, and a mini toy train.",
                "https://www.tamilnadutourism.tn.gov.in", "North Lake Rd, Kathadimattam, Ooty 643004",
                11.4072, 76.6874, 90, 60.0, "Central Ooty & Heritage"
        ));
        ooty.add(new PlaceItemDto(
                "ooty-doddabetta", "Doddabetta Peak & Telescope House", "Nature & Viewpoint",
                "Doddabetta, Nilgiris, Tamil Nadu, India", "4.6",
                null,
                "Highest summit in the Nilgiri Mountains at 2,637m offering sweeping vistas of the Coimbatore plains and dense shola forests.",
                "https://www.tamilnadutourism.tn.gov.in", "Ooty-Kotagiri Rd, Doddabetta 643002",
                11.4011, 76.7348, 90, 20.0, "Mountain Peaks & Tea Trails"
        ));
        ooty.add(new PlaceItemDto(
                "ooty-rose-garden", "Government Rose Garden", "Nature & Park",
                "Elk Hill, Ooty, Tamil Nadu, India", "4.6",
                null,
                "One of India's largest rose gardens spanning five terraced slopes with over 20,000 varieties of roses in full bloom.",
                "https://www.tamilnadutourism.tn.gov.in", "Harinagar, Elk Hill, Ooty 643001",
                11.4068, 76.7165, 75, 40.0, "Central Ooty & Heritage"
        ));
        ooty.add(new PlaceItemDto(
                "ooty-pykara", "Pykara Falls & Pykara Lake", "Nature & Boating",
                "Pykara, Nilgiris, Tamil Nadu, India", "4.6",
                null,
                "Pristine sacred river cascading down tiered waterfalls into Pykara Lake surrounded by protected pine and shola forests.",
                "https://www.tamilnadutourism.tn.gov.in", "Pykara, Sholur, Tamil Nadu 643224",
                11.5167, 76.6000, 150, 100.0, "Pykara & Lakes Wilderness"
        ));
        ooty.add(new PlaceItemDto(
                "ooty-tea-museum", "Tea Factory & Tea Museum", "Culture & Food",
                "Dodabetta Road, Ooty, Tamil Nadu, India", "4.4",
                null,
                "Educational factory tour demonstrating orthodox CTC tea processing from green leaf to packaging, complete with free cardamom tea tasting.",
                "https://www.tamilnadutourism.tn.gov.in", "Dodabetta Rd, Mel Kaundi, Ooty 643002",
                11.4089, 76.7300, 75, 30.0, "Mountain Peaks & Tea Trails"
        ));
        ooty.add(new PlaceItemDto(
                "ooty-avalanche", "Avalanche Lake & Sanctuary", "Nature & Eco-Tourism",
                "Avalanche, Nilgiris, Tamil Nadu, India", "4.7",
                null,
                "Unspoiled pristine wilderness lake surrounded by rolling grasslands, trout hatcheries, and eco-safaris.",
                "https://www.tamilnadutourism.tn.gov.in", "Avalanche, Tamil Nadu 643004",
                11.3000, 76.5833, 180, 150.0, "Pykara & Lakes Wilderness"
        ));
        REGISTRY.put("ooty", ooty);
    }

    public static List<PlaceItemDto> getCuratedPlaces(String destination) {
        if (destination == null || destination.trim().isBlank()) {
            return Collections.emptyList();
        }
        String destLower = destination.toLowerCase().trim();
        for (Map.Entry<String, List<PlaceItemDto>> entry : REGISTRY.entrySet()) {
            if (destLower.contains(entry.getKey())) {
                return new ArrayList<>(entry.getValue());
            }
        }
        return Collections.emptyList();
    }

    public static boolean hasCuratedPlaces(String destination) {
        if (destination == null || destination.trim().isBlank()) {
            return false;
        }
        String destLower = destination.toLowerCase().trim();
        for (String key : REGISTRY.keySet()) {
            if (destLower.contains(key)) {
                return true;
            }
        }
        return false;
    }
}
