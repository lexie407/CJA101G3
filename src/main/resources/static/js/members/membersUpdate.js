const taiwanData = {
    "台北市": ["中正區", "大同區", "中山區", "松山區", "大安區", "萬華區", "信義區", "士林區", "北投區", "內湖區", "南港區", "文山區"],
    "新北市": ["板橋區", "三重區", "中和區", "永和區", "新莊區", "新店區", "土城區", "蘆洲區", "樹林區", "汐止區", "鶯歌區", "三峽區", "淡水區", "瑞芳區", "五股區", "泰山區", "林口區", "八里區", "深坑區", "石碇區", "坪林區", "三芝區", "石門區", "烏來區"],
    "桃園市": ["桃園區", "中壢區", "平鎮區", "八德區", "楊梅區", "蘆竹區", "大溪區", "龍潭區", "龜山區", "大園區", "觀音區", "新屋區", "復興區"],
    "台中市": ["中區", "東區", "南區", "西區", "北區", "北屯區", "西屯區", "南屯區", "太平區", "大里區", "霧峰區", "烏日區", "豐原區", "后里區", "石岡區", "東勢區", "和平區", "新社區", "潭子區", "大雅區", "神岡區", "大肚區", "沙鹿區", "龍井區", "梧棲區", "清水區", "大甲區", "外埔區", "大安區"],
    "台南市": ["中西區", "東區", "南區", "北區", "安平區", "安南區", "永康區", "歸仁區", "新化區", "左鎮區", "玉井區", "楠西區", "南化區", "仁德區", "關廟區", "龍崎區", "官田區", "麻豆區", "佳里區", "西港區", "七股區", "將軍區", "學甲區", "北門區", "新營區", "後壁區", "白河區", "東山區", "六甲區", "下營區", "柳營區", "鹽水區", "善化區", "大內區", "山上區", "新市區", "安定區"],
    "高雄市": ["新興區", "前金區", "苓雅區", "鹽埕區", "鼓山區", "旗津區", "前鎮區", "三民區", "楠梓區", "小港區", "左營區", "仁武區", "大社區", "岡山區", "路竹區", "阿蓮區", "田寮區", "燕巢區", "橋頭區", "梓官區", "彌陀區", "永安區", "湖內區", "鳳山區", "大寮區", "林園區", "鳥松區", "大樹區", "旗山區", "美濃區", "六龜區", "內門區", "杉林區", "甲仙區", "桃源區", "那瑪夏區", "茂林區"],
    "基隆市": ["仁愛區", "信義區", "中正區", "中山區", "安樂區", "暖暖區", "七堵區"],
    "新竹市": ["東區", "北區", "香山區"],
    "新竹縣": ["竹北市", "竹東鎮", "新埔鎮", "關西鎮", "湖口鄉", "新豐鄉", "芎林鄉", "寶山鄉", "北埔鄉", "峨眉鄉", "橫山鄉", "尖石鄉", "五峰鄉"],
    "苗栗縣": ["苗栗市", "頭份市", "竹南鎮", "後龍鎮", "通霄鎮", "苑裡鎮", "卓蘭鎮", "造橋鄉", "西湖鄉", "頭屋鄉", "公館鄉", "大湖鄉", "泰安鄉", "銅鑼鄉", "三義鄉", "南庄鄉", "獅潭鄉"],
    "彰化縣": ["彰化市", "和美鎮", "鹿港鎮", "溪湖鎮", "二林鎮", "田中鎮", "北斗鎮", "花壇鄉", "芬園鄉", "員林市", "永靖鄉", "埔心鄉", "溪州鄉", "竹塘鄉", "埤頭鄉", "二水鄉", "大城鄉", "芳苑鄉", "福興鄉", "秀水鄉", "伸港鄉", "大村鄉"],
    "南投縣": ["南投市", "草屯鎮", "埔里鎮", "竹山鎮", "集集鎮", "名間鄉", "鹿谷鄉", "中寮鄉", "魚池鄉", "國姓鄉", "水里鄉", "信義鄉", "仁愛鄉"],
    "雲林縣": ["斗六市", "斗南鎮", "虎尾鎮", "西螺鎮", "土庫鎮", "北港鎮", "古坑鄉", "大埤鄉", "莿桐鄉", "林內鄉", "二崙鄉", "崙背鄉", "麥寮鄉", "東勢鄉", "褒忠鄉", "台西鄉", "元長鄉", "四湖鄉", "口湖鄉", "水林鄉"],
    "嘉義市": ["東區", "西區"],
    "嘉義縣": ["太保市", "朴子市", "布袋鎮", "大林鎮", "民雄鄉", "溪口鄉", "新港鄉", "六腳鄉", "東石鄉", "義竹鄉", "鹿草鄉", "水上鄉", "中埔鄉", "竹崎鄉", "梅山鄉", "番路鄉", "大埔鄉", "阿里山鄉"],
    "屏東縣": ["屏東市", "潮州鎮", "東港鎮", "恆春鎮", "萬丹鄉", "長治鄉", "麟洛鄉", "九如鄉", "里港鄉", "鹽埔鄉", "高樹鄉", "萬巒鄉", "內埔鄉", "竹田鄉", "新埤鄉", "枋寮鄉", "新園鄉", "崁頂鄉", "林邊鄉", "南州鄉", "佳冬鄉", "琉球鄉", "車城鄉", "滿州鄉", "枋山鄉", "三地門鄉", "霧台鄉", "瑪家鄉", "泰武鄉", "來義鄉", "春日鄉", "獅子鄉", "牡丹鄉"],
    "宜蘭縣": ["宜蘭市", "羅東鎮", "蘇澳鎮", "頭城鎮", "礁溪鄉", "壯圍鄉", "員山鄉", "冬山鄉", "五結鄉", "三星鄉", "大同鄉", "南澳鄉"],
    "花蓮縣": ["花蓮市", "鳳林鎮", "玉里鎮", "新城鄉", "吉安鄉", "壽豐鄉", "光復鄉", "豐濱鄉", "瑞穗鄉", "萬榮鄉", "富里鄉", "秀林鄉", "卓溪鄉"],
    "台東縣": ["台東市", "成功鎮", "關山鎮", "卑南鄉", "大武鄉", "太麻里鄉", "東河鄉", "長濱鄉", "鹿野鄉", "池上鄉", "綠島鄉", "延平鄉", "海端鄉", "達仁鄉", "金峰鄉", "蘭嶼鄉"],
    "澎湖縣": ["馬公市", "湖西鄉", "白沙鄉", "西嶼鄉", "望安鄉", "七美鄉"],
    "金門縣": ["金城鎮", "金湖鎮", "金沙鎮", "金寧鄉", "烈嶼鄉", "烏坵鄉"],
    "連江縣": ["南竿鄉", "北竿鄉", "莒光鄉", "東引鄉"]
};

  // Elements
  const citySelect = document.getElementById('citySelect');
  const districtSelect = document.getElementById('districtSelect');
  const addrDetailInput = document.getElementById('addrDetailInput');
  const memAddrInput = document.getElementById('memAddr');
  const memAvatarInput = document.getElementById('memAvatarInput');
  const avatarPreview = document.getElementById('avatarPreview');
  const avatarPlaceholder = avatarPreview?.nextElementSibling;
  const memAvatarFrameInput = document.getElementById('memAvatarFrame');
  const avatarFramePreview = document.getElementById('avatarFramePreview');
  const avatarFramePlaceholder = avatarFramePreview?.nextElementSibling;
  
  // Populate city select with placeholder
  function populateCities() {
      citySelect.innerHTML = '';
      Object.keys(taiwanData).forEach(city => {
          const opt = document.createElement('option');
          opt.value = city;
          opt.textContent = city || '──請選縣市──';
          citySelect.appendChild(opt);
      });
  }
  
  // Populate district based on cityKey
  function populateDistricts(cityKey) {
      districtSelect.innerHTML = '';
      const list = taiwanData[cityKey] || [""];
      list.forEach(dist => {
          const opt = document.createElement('option');
          opt.value = dist;
          opt.textContent = dist || '──請選鄉鎮──';
          districtSelect.appendChild(opt);
      });
  }
  
  // Update hidden memAddr
  function updateMemAddr() {
      const city = citySelect.value;
      const dist = districtSelect.value;
      const detail = addrDetailInput.value.trim();
      memAddrInput.value = (city && dist && detail) ? city + dist + detail : '';
  }
  
  // Avatar preview initialization
function initAvatarPreview() {
    if (avatarPreview && avatarPreview.src && !avatarPreview.src.includes("null")) {
        avatarPreview.style.display = 'block';
        if (avatarPlaceholder) avatarPlaceholder.style.display = 'none';
    } else {
        avatarPreview.style.display = 'none';
        if (avatarPlaceholder) avatarPlaceholder.style.display = 'inline-block';
    }
    memAvatarInput.addEventListener('change', function () {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                avatarPreview.src = e.target.result;
                avatarPreview.style.display = 'block';
                if (avatarPlaceholder) avatarPlaceholder.style.display = 'none';
            };
            reader.readAsDataURL(file);
        } else {
            avatarPreview.src = '';
            avatarPreview.style.display = 'none';
            if (avatarPlaceholder) avatarPlaceholder.style.display = 'inline-block';
        }
    });
    
    if (avatarFramePreview && avatarFramePreview.src && !avatarFramePreview.src.includes("null")) {
        avatarFramePreview.style.display = 'block';
        if (avatarFramePlaceholder) avatarFramePlaceholder.style.display = 'none';
    } else {
        avatarFramePreview.style.display = 'none';
        if (avatarFramePlaceholder) avatarFramePlaceholder.style.display = 'inline-block';
    }
    memAvatarFrameInput.addEventListener('change', function () {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                avatarFramePreview.src = e.target.result;
                avatarFramePreview.style.display = 'block';
                if (avatarFramePlaceholder) avatarFramePlaceholder.style.display = 'none';
            };
            reader.readAsDataURL(file);
        } else {
            avatarFramePreview.src = '';
            avatarFramePreview.style.display = 'none';
            if (avatarFramePlaceholder) avatarFramePlaceholder.style.display = 'inline-block';
        }
    });
}

// Form validation
  function initFormValidation() {
      document.querySelector('form').addEventListener('submit', function(e) {
          let valid = true;
          let first = null;
          const fields = [
              {id:'memName',name:'會員姓名'},{id:'memGender',name:'性別'},{id:'memBirthDate',name:'生日'},
              {id:'memMobile',name:'手機'},{id:'memEmail',name:'Email'},{id:'citySelect',name:'縣市'},
              {id:'districtSelect',name:'鄉鎮市區'},{id:'addrDetailInput',name:'街道地址'},{id:'memUsername',name:'使用者暱稱'}
          ];
          fields.forEach(f => {
              const el = document.getElementById(f.id);
              if (!el.value.trim()) {
                  valid = false;
                  if (!first) first = el;
                  el.style.borderColor = 'red';
                  el.setCustomValidity(`請填寫${f.name}`);
              } else {
                  el.style.borderColor = '';
                  el.setCustomValidity('');
              }
          });
          if (!valid) {
              e.preventDefault();
              first.focus();
              alert('請填寫所有必填欄位後再送出');
          }
      });
  }
  
  // Main init
window.addEventListener('DOMContentLoaded', () => {
    // 1. Build city and initial district placeholder
    populateCities();
    populateDistricts('');

    // 2. Parse full memAddr (hidden) into city/district/detail
    let full = (memAddrInput.value || '').trim();
    const normalized = full;
    console.log('完整地址:', full);
    let initCity = '';
    let initDist = '';
    let initDetail = '';

    if (full) {
        // a. find longest matching city
        Object.keys(taiwanData).forEach(c => {
            if (c && normalized.startsWith(c) && c.length > initCity.length) {
                initCity = c;
                console.log('匹配到 city:', initCity);
            }
        });
        if (initCity) {
            const rest = full.slice(initCity.length);
            console.log('City 後剩:', rest);
            // b. find longest matching district
            (taiwanData[initCity] || []).forEach(d => {
                if (d && rest.startsWith(d) && d.length > initDist.length) {
                    initDist = d;
                    console.log('匹配到 district:', initDist);
                }
            });
            // c. remaining is detail
            initDetail = rest.slice(initDist.length);
            console.log('剩下 detail:', initDetail);
        }
    }

// 3. Fill UI with parsed values Fill UI with parsed values
    citySelect.value = initCity;
    populateDistricts(initCity);
    districtSelect.value = initDist;
    addrDetailInput.value = initDetail;

    // 4. Sync hidden field
    updateMemAddr();

    // 5. Bind events
    citySelect.addEventListener('change', e => {
        populateDistricts(e.target.value);
        updateMemAddr();
    });
    districtSelect.addEventListener('change', updateMemAddr);
    addrDetailInput.addEventListener('input', updateMemAddr);

    // 6. Initialize avatar & validation
    initAvatarPreview();
    initFormValidation();
});
