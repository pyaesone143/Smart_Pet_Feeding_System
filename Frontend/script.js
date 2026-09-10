(function() {
  console.log("Pet Feeder script loaded");

  // ============================================
  // ELEMENTS
  // ============================================
  const feedBtn = document.getElementById('feedButton');
  const feedStatus = document.getElementById('feedStatus');
  const foodProgressBar = document.getElementById('foodProgressBar');
  const foodPercentage = document.getElementById('foodPercentage');
  const foodRemaining = document.getElementById('foodRemaining');
  const foodLoading = document.getElementById('foodLoading');
  const foodData = document.getElementById('foodData');
  const foodError = document.getElementById('foodError');
  const profileCircle = document.getElementById('profileCircle');
  const profileName = document.getElementById('profileName');

  const API_BASE = 'http://localhost:8080';
   
  // ============================================
  // LOAD PROFILE FROM LOCALSTORAGE (set on login/signup)
  // Falls back to "Admin" if no user logged in
  // ============================================
  function loadProfile() {
    try {
      const user = JSON.parse(localStorage.getItem('currentUser'));

      if (user && user.name) {
        const name = user.name;
        const initial = name.charAt(0).toUpperCase();

        profileName.textContent = name;
        profileCircle.textContent = initial;
        console.log('Profile loaded:', name);
      } else {
        // Default to Admin
        profileName.textContent = 'Admin';
        profileCircle.textContent = 'A';
        console.log('Default profile: Admin');
      }
    } catch (error) {
      console.error('Error loading profile:', error);
      // Default to Admin on error
      profileName.textContent = 'Admin';
      profileCircle.textContent = 'A';
    }
  }

  // ============================================
  // UPDATE PROFILE AFTER LOGIN/SIGNUP
  // ============================================
  function updateProfileAfterLogin(userData) {
    if (userData && userData.name) {
      const name = userData.name;
      const initial = name.charAt(0).toUpperCase();

      profileName.textContent = name;
      profileCircle.textContent = initial;

      // Save to localStorage
      localStorage.setItem('currentUser', JSON.stringify(userData));
      console.log('Profile updated:', name);
    }
  }

  // Load saved user when page opens

 // ============================================
// LOAD FOOD LEVEL FROM API
// ============================================

function loadFoodLevel() {

    const currentUser =
        localStorage.getItem('currentUser');

    // ========================================
    // NO USER LOGGED IN
    // ========================================

    if (!currentUser) {
        resetFoodLevel();
        return;
    }


    // ========================================
    // SHOW LOADING
    // ========================================

    foodLoading.style.display = 'block';
    foodData.style.display = 'none';
    foodError.style.display = 'none';


    // ========================================
    // GET FOOD LEVEL
    // ========================================

    fetch(API_BASE + '/api/food')
        .then(response => {

            if (!response.ok) {

                throw new Error(
                    'Food API error: ' +
                    response.status
                );
            }

            return response.json();
        })

        .then(data => {

            console.log(
                'Food from API:',
                data
            );


            // ==================================
            // GET THE LATEST USER DATA
            // FROM LOCALSTORAGE
            // ==================================

            const latestUser =
                JSON.parse(
                    localStorage.getItem(
                        'currentUser'
                    )
                );


            if (
                !latestUser ||
                !latestUser.user_id
            ) {

                resetFoodLevel();
                return;
            }


            // ==================================
            // GET LATEST MAX FOOD AMOUNT
            // ==================================

            const maxFood =
                Number(
                    latestUser.max_food_amount
                );


            if (!maxFood || maxFood <= 0) {

                console.error(
                    'Invalid max food amount:',
                    maxFood
                );

                return;
            }


            console.log(
                'Latest Maximum Food Amount:',
                maxFood + 'g'
            );


            // ==================================
            // REMAINING FOOD
            // ==================================

            const remainingFood =
                Number(data);


            // ==================================
            // CALCULATE PERCENTAGE
            // ==================================

            const percentage =
                Math.min(
                    (remainingFood / maxFood) * 100,
                    100
                );


            // ==================================
            // UPDATE UI
            // ==================================

            foodProgressBar.style.width =
                percentage + '%';

            foodPercentage.textContent =
                Math.round(percentage) + '%';

            foodRemaining.textContent =
                Math.round(remainingFood) +
                ' g remaining';

            foodTotal.textContent =
                maxFood + ' g total';


            // ==================================
            // SHOW DATA
            // ==================================

            foodLoading.style.display = 'none';
            foodData.style.display = 'block';
            foodError.style.display = 'none';

        })

        .catch(error => {

            console.error(
                'Food API Error:',
                error
            );

            foodLoading.style.display = 'none';
            foodData.style.display = 'none';
            foodError.style.display = 'block';

            foodError.textContent =
                '❌ ' + error.message;
        });
}

 function resetFoodLevel() {

    const currentUser = localStorage.getItem('currentUser');

    let maxFood = 0;

    if (currentUser) {
        const user = JSON.parse(currentUser);
        maxFood = Number(user.max_food_amount) || 0;
    }

    foodProgressBar.style.width = '0%';
    foodPercentage.textContent = '0%';
    foodRemaining.textContent = '0 g remaining';
    foodTotal.textContent = maxFood + ' g total';

    foodLoading.style.display = 'none';
    foodData.style.display = 'block';
    foodError.style.display = 'none';
}

  // ============================================
  // ============================================
// FEED PET
// ============================================
async function feedPet() {
  feedBtn.disabled = true;

  feedStatus.innerText = '⏳ Sending command...';
  feedStatus.style.color = 'rgba(255, 255, 255, 0.6)';

  const currentUser = JSON.parse(
    localStorage.getItem('currentUser')
  );

  if (!currentUser || !currentUser.user_id) {
    feedStatus.innerText = '❌ Please login first.';
    feedStatus.style.color = 'rgba(255, 150, 150, 0.8)';
    feedBtn.disabled = false;
    return;
  }

  const userId = currentUser.user_id;

  console.log('Current User ID:', userId);

  try {
    const response = await fetch(
      API_BASE + '/api/feed',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          user_id: userId
        })
      }
    );

    const data = await response.text();

    console.log('Spring Boot / ESP32 Response:', data);

    if (!response.ok) {
      throw new Error(data);
    }

    feedStatus.innerHTML =
      '✅ Feeding started successfully!';

    feedStatus.style.color =
      'rgba(180, 220, 170, 0.7)';

    setTimeout(() => {
      loadFoodLevel();
    }, 2000);

  } catch (error) {
    console.error(
      'Feed API connection failed:',
      error
    );

    feedStatus.innerText =
      '❌ Failed to start feeding';

    feedStatus.style.color =
      'rgba(255, 150, 150, 0.8)';
  }

  setTimeout(() => {
    feedBtn.disabled = false;

    feedStatus.innerHTML =
      '<span style="opacity:0.3; font-weight:400;">press to serve</span>';

    feedStatus.style.color =
      'rgba(255,255,255,0.5)';
  }, 3000);
}

  // ============================================
  // LOAD HISTORY
  // ============================================
  function loadHistory() {

  const historyList = document.getElementById('historyList');
  const historyEmpty = document.getElementById('historyEmpty');

  // Get currently logged-in user
  const currentUser = JSON.parse(
    localStorage.getItem('currentUser')
  );

  // Check login
  if (!currentUser || !currentUser.user_id) {

    historyList.innerHTML = '';
    historyList.classList.add('hidden');

    historyEmpty.style.display = 'block';
    historyEmpty.innerText = 'Please login first.';

    return;
  }

  const userId = currentUser.user_id;

  console.log('Loading history for User ID:', userId);

  // Show loading message
  historyList.innerHTML = '';
  historyList.classList.add('hidden');

  historyEmpty.style.display = 'block';
  historyEmpty.innerText = 'Loading history...';

  // ============================================
  // GET USER FEEDING HISTORY
  // ============================================
  fetch(
    API_BASE + '/api/history/user/' + userId
  )
    .then(response => {

      if (!response.ok) {
        throw new Error(
          'History API error: ' + response.status
        );
      }

      return response.json();
    })

    .then(data => {

      console.log('History data:', data);

      // ============================================
      // NO HISTORY
      // ============================================
      if (!data || data.length === 0) {

        historyList.innerHTML = '';
        historyList.classList.add('hidden');

        historyEmpty.style.display = 'block';
        historyEmpty.innerText =
          'No feeding records yet';

        return;
      }

      // ============================================
      // SHOW HISTORY
      // ============================================
      historyEmpty.style.display = 'none';
      historyList.classList.remove('hidden');

      data.forEach(item => {

  const li = document.createElement('li');

  li.className = 'history-item';

  console.log('History item:', item);
  console.log('Feeding time:', item.feedingTime);
  console.log('Amount:', item.amount);

  // ==============================
  // FEEDING TIME
  let feedingTime = item.feedingTime;

// ============================================
// FORMAT FEEDING DATE & TIME
// ============================================
if (feedingTime) {

  let feedingTime = item.feedingTime;

console.log("RAW feedingTime:", feedingTime);
console.log("feedingTime type:", typeof feedingTime);

if (
  feedingTime !== null &&
  feedingTime !== undefined &&
  feedingTime !== "" &&
  feedingTime !== 0 &&
  feedingTime !== "0"
) {

  const date = new Date(feedingTime);

  if (!isNaN(date.getTime())) {

    feedingTime = date.toLocaleString("en-US", {
      year: "numeric",
      month: "numeric",
      day: "numeric",
      hour: "numeric",
      minute: "2-digit",
      second: "2-digit",
      hour12: true
    });

  } else {

    feedingTime = "Invalid date";

  }

} else {

  feedingTime = "No date";

}
  // ==============================
  // FEEDING AMOUNT
  // ==============================
  let amount = Number(item.amount);

  if (isNaN(amount)) {
    amount = 0;
  }

  // ==============================
  // DISPLAY HISTORY
  // ==============================
  li.innerHTML = `
    <div class="history-time">
      <i class="fas fa-clock"></i>
       <span style="color: #f9efef;">${feedingTime}</span>
    </div>

    <div class="history-amount">
      <i class="fas fa-weight-hanging"></i>
      <strong>${amount.toFixed(1)} g</strong>
    </div>
  `;

  historyList.appendChild(li);
}
});
    })

    .catch(error => {

      console.error(
        'History load error:',
        error
      );

      historyList.innerHTML = '';
      historyList.classList.add('hidden');

      historyEmpty.style.display = 'block';
      historyEmpty.innerText =
        'Unable to load history';
    });
}

  // ============================================
  // INITIALIZE
  // ============================================
  document.addEventListener('DOMContentLoaded', function() {
    loadProfile();
    loadFoodLevel();

    // Refresh food level every 3 seconds
    setInterval(loadFoodLevel, 3000);
  });

  // ============================================
  // EVENT LISTENERS
  // ============================================
  feedBtn.addEventListener('click', feedPet);
  window.feedPet = feedPet;

  // ============================================
  // OVERLAYS CONTROL
  // ============================================
 const authOverlay =
  document.getElementById('authOverlay');

const historyOverlay =
  document.getElementById('historyOverlay');

const openAuthBtn =
  document.getElementById('openAccountBtn');

const closeAuthBtn =
  document.getElementById('closeOverlayBtn');

const closeHistoryBtn =
  document.getElementById('closeHistoryBtn');


  function openModal(modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
  }

  function closeModal(modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
  }

  openAuthBtn.addEventListener('click', () => openModal(authOverlay));
  closeAuthBtn.addEventListener('click', () => closeModal(authOverlay));
  
  closeHistoryBtn.addEventListener('click', () => closeModal(historyOverlay));
 
  window.addEventListener('click', function(e) {
    if (e.target === authOverlay) closeModal(authOverlay);
    if (e.target === historyOverlay) closeModal(historyOverlay);
  });

  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
      closeModal(authOverlay);
      closeModal(historyOverlay);
    }
  });

  // ============================================
  // AUTH TOGGLE
  // ============================================
  const loginForm = document.getElementById('overlayLoginForm');
const loginMsg = document.getElementById('overlayLoginMessage');

loginForm.classList.remove('hidden');
loginMsg.innerText = '';

  // ============================================
  // LOGIN - REAL DATA FROM DATABASE
  // ============================================
  document.getElementById('overlayLoginBtn').addEventListener('click', function(e) {
    e.preventDefault();
    const email = document.getElementById('overlayLoginEmail').value.trim();
    const password = document.getElementById('overlayLoginPassword').value.trim();

    if (!email || !password) {
      loginMsg.innerText = '⚠️ Please fill in all fields.';
      loginMsg.style.color = 'rgba(255,200,150,0.8)';
      return;
    }

    loginMsg.innerText = '⏳ Logging in...';
    loginMsg.style.color = 'rgba(255,255,255,0.6)';

    fetch(API_BASE + '/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })
    .then(response => {
      if (!response.ok) {
        return response.text().then(text => { throw new Error(text || 'Login failed'); });
      }
      return response.json();
    })
    .then(userData => {
      console.log('Login successful, user data:', userData);

      //  SAVE CURRENT LOGGED-IN USER //
      localStorage.setItem('currentUser', JSON.stringify(userData));
      localStorage.setItem('userId', userData.user_id);

       console.log('Current user saved:', userData);
       console.log('Current User ID:', userData.user_id);
       //current user to backend
       fetch(API_BASE + '/api/current-user/' + userData.user_id, {
  method: 'POST'
})
.then(response => response.text())
.then(data => {
  console.log('Backend current user:', data);
})
.catch(error => {
  console.error('Failed to set current user:', error);
});
//

      // Update profile with real user data from database
      updateProfileAfterLogin(userData);
      

      loginMsg.innerText = '✅ Login successful!';
      loginMsg.style.color = 'rgba(180,220,170,0.8)';

      // Clear input fields
      document.getElementById('overlayLoginEmail').value = '';
      document.getElementById('overlayLoginPassword').value = '';

      // Close overlay after success
      setTimeout(() => {
        closeModal(authOverlay);
      }, 1000);
    })
    .catch(error => {
      loginMsg.innerText = '❌ ' + (error.message || 'Connection failed!');
      loginMsg.style.color = 'rgba(255,150,150,0.8)';
      console.error('Login error:', error);
    });
  });
  //logout 
  const logoutButton = document.getElementById('logoutButton');

if (logoutButton) {
  logoutButton.addEventListener('click', function () {

    // Remove logged-in user
    localStorage.removeItem('currentUser');
    localStorage.removeItem('user');
    localStorage.removeItem('userId');
    resetFoodLevel();

    // Clear profile
    profileName.textContent = 'Admin';
    profileCircle.textContent = 'A';

    console.log('User logged out');

    // Close login overlay
    closeModal(authOverlay);
  });
}

  // ============================================
  // SIGNUP - REAL DATA TO DATABASE
  // ============================================

      // Auto-login after signup
      if (userData && userData.name) {
        updateProfileAfterLogin(userData);
      }

      // Close overlay after success
      setTimeout(() => {
        closeModal(authOverlay);
      }, 1500);
    })
    .catch(error => {
      signupMsg.innerText = '❌ ' + (error.message || 'Connection failed!');
      signupMsg.style.color = 'rgba(255,150,150,0.8)';
      console.error('Signup error:', error);
    });
  });

  // ============================================
// SETTINGS OVERLAY
// ============================================

const openSettingBtn = document.getElementById("openSettingBtn");
const settingOverlay = document.getElementById("settingOverlay");
const closeSettingBtn = document.getElementById("closeSettingBtn");

// Open Settings
openSettingBtn.addEventListener("click", () => {
  settingOverlay.classList.add("active");
});

// Close Settings
closeSettingBtn.addEventListener("click", () => {
  settingOverlay.classList.remove("active");
});


// ============================================
// HISTORY FROM SETTINGS
// ============================================

const openHistoryFromSettingBtn =
  document.getElementById("openHistoryFromSettingBtn");


openHistoryFromSettingBtn.addEventListener("click", () => {

  // Load feeding history
  loadHistory();

  // Close Settings
  settingOverlay.classList.remove("active");

  // Open History
  historyOverlay.classList.add("active");
});

// ============================================
// SET FOOD AMOUNT
// ============================================

const openSetAmountBtn =
  document.getElementById("openSetAmountBtn");

const setAmountOverlay =
  document.getElementById("setAmountOverlay");

const closeSetAmountBtn =
  document.getElementById("closeSetAmountBtn");

const foodAmountInput =
  document.getElementById("foodAmountInput");

const saveAmountBtn =
  document.getElementById("saveAmountBtn");


// ============================================
// CURRENT TARGET FOOD AMOUNT
// ============================================

let targetFoodAmount = 100;

const savedCurrentUser =
  JSON.parse(
    localStorage.getItem("currentUser")
  );

if (
  savedCurrentUser &&
  savedCurrentUser.max_food_amount > 0
) {
  targetFoodAmount =
    Number(savedCurrentUser.max_food_amount);
}


// ============================================
// OPEN SET AMOUNT OVERLAY
// ============================================

openSetAmountBtn.addEventListener(
  "click",
  function () {

    foodAmountInput.value =
      targetFoodAmount;

    setAmountOverlay.classList.add(
      "active"
    );
  }
);


// ============================================
// CLOSE SET AMOUNT OVERLAY
// ============================================

closeSetAmountBtn.addEventListener(
  "click",
  function () {

    setAmountOverlay.classList.remove(
      "active"
    );
  }
);


// ============================================
// SAVE TARGET FOOD AMOUNT
// ============================================

saveAmountBtn.addEventListener(
  "click",
  async function () {

    const amount =
      Number(foodAmountInput.value);

    // ========================================
    // VALIDATE AMOUNT
    // ========================================

    if (!amount || amount <= 0) {

      alert(
        "Please enter a valid food amount."
      );

      return;
    }


    // ========================================
    // GET CURRENT USER
    // ========================================

    const currentUser =
      JSON.parse(
        localStorage.getItem("currentUser")
      );

    if (
      !currentUser ||
      !currentUser.user_id
    ) {

      alert(
        "Please login first."
      );

      return;
    }


    const userId =
      currentUser.user_id;


    console.log(
      "Current User ID:",
      userId
    );

    console.log(
      "New Target Amount:",
      amount + "g"
    );


    try {

      // ======================================
      // SEND TO SPRING BOOT
      // ======================================

      const response =
        await fetch(
          API_BASE + "/api/food-limit",
          {
            method: "POST",

            headers: {
              "Content-Type":
                "application/json"
            },

            body: JSON.stringify({
              userId: userId,
              max_food_amount: amount
            })
          }
        );


      // ======================================
      // CHECK RESPONSE
      // ======================================

      if (!response.ok) {

        const errorText =
          await response.text();

        throw new Error(
          errorText ||
          "Failed to save food amount"
        );
      }


      // ======================================
      // GET BACKEND RESPONSE
      // ======================================

      const result =
        await response.json();


      console.log(
        "Food Limit Response:",
        result
      );


      // ======================================
      // CHECK SUCCESS
      // ======================================

      if (!result.success) {

        throw new Error(
          result.message ||
          result.status ||
          "Failed to save food limit"
        );
      }


      // ======================================
      // GET NEW VALUE FROM BACKEND
      // ======================================

      const newMaxFood =
        Number(
          result.max_food_amount
        );


      console.log(
        "Backend returned:",
        newMaxFood + "g"
      );


      // ======================================
      // UPDATE CURRENT USER
      // ======================================

      currentUser.max_food_amount =
        newMaxFood;


      // ======================================
      // SAVE TO LOCALSTORAGE
      // ======================================

      localStorage.setItem(
        "currentUser",
        JSON.stringify(currentUser)
      );


      // ======================================
      // VERIFY LOCALSTORAGE
      // ======================================

      const savedUser =
        JSON.parse(
          localStorage.getItem("currentUser")
        );


      console.log(
        "LOCALSTORAGE AFTER SAVE:",
        savedUser
      );

      console.log(
        "LOCALSTORAGE max_food_amount:",
        savedUser.max_food_amount
      );


      // ======================================
      // UPDATE TARGET FOOD AMOUNT
      // ======================================

      targetFoodAmount =
        newMaxFood;


      console.log(
        "targetFoodAmount:",
        targetFoodAmount
      );


      // ======================================
      // UPDATE UI DIRECTLY
      // ======================================

      foodTotal.textContent =
        newMaxFood + " g total";


      // ======================================
      // CLOSE OVERLAY
      // ======================================

      setAmountOverlay.classList.remove(
        "active"
      );


      // ======================================
      // SUCCESS MESSAGE
      // ======================================

      if (
        result.esp32_connected === false
      ) {

        alert(
          "Food amount saved successfully!\n\n" +
          "ESP32 is not connected, but the new amount " +
          "was saved to the database."
        );

      } else {

        alert(
          "Food amount saved successfully!"
        );
      }


    } catch (error) {

      console.error(
        "Food limit error:",
        error
      );

      alert(
        "Failed to save food amount.\n\n" +
        error.message
      );
    }
  }
);

   

}());
