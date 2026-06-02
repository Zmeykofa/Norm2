document.addEventListener("DOMContentLoaded", () => {
  
  // --- НАВИГАЦИЯ И МОБИЛЬНОЕ МЕНЮ ---
  const mobileMenuBtn = document.getElementById("mobileMenuBtn");
  const navLinksContainer = document.querySelector(".nav-links");
  
  if (mobileMenuBtn) {
    mobileMenuBtn.addEventListener("click", () => {
      navLinksContainer.classList.toggle("active");
      const icon = mobileMenuBtn.querySelector("span");
      icon.textContent = navLinksContainer.classList.contains("active") ? "close" : "menu";
    });
  }

  // Скролл к секции скачивания
  const scrollDownloadBtn = document.getElementById("scrollDownloadBtn");
  if (scrollDownloadBtn) {
    scrollDownloadBtn.addEventListener("click", () => {
      document.getElementById("platforms").scrollIntoView({ behavior: "smooth" });
    });
  }

  // --- ИНТЕРАКТИВНЫЕ МАКЕТЫ (Features Showcase) ---
  const showcaseTabs = document.querySelectorAll(".showcase-tab");
  const mockupScreen = document.getElementById("mockupScreen");

  const mockupTemplates = {
    timers: `
      <div class="mockup-screen-layout animate-fade-in">
        <div class="mockup-card-title">
          <span class="material-icons-outlined">timer</span>
          <span>Секундомеры реального времени</span>
        </div>
        <div class="mockup-card">
          <div class="mockup-item">
            <span style="font-weight:600;">Укладка асфальтобетона</span>
            <span class="demo-timer-span" id="mockupTickingTimer" style="color:var(--success); font-family:monospace; font-weight:700;">00:05:43</span>
          </div>
          <p style="font-size:0.7rem; color:var(--text-secondary); margin-top:2px;">Ресурсы: Асфальтоукладчик • 👥 4 человека</p>
          <div style="display:flex; gap:8px; margin-top:8px;">
            <button class="mockup-btn" id="mockupSplitBtn">Разделить</button>
            <button class="mockup-btn" style="background:var(--danger);" id="mockupStopBtn">Стоп</button>
          </div>
        </div>
        <div class="mockup-card" style="border-style:solid; border-color:rgba(255,255,255,0.05); background:rgba(0,0,0,0.15);">
          <div class="mockup-item" style="color:var(--text-secondary);">
            <span>Выемка грунта в отвал</span>
            <span style="font-weight:600;">01:45:00</span>
          </div>
        </div>
      </div>
    `,
    db: `
      <div class="mockup-screen-layout animate-fade-in">
        <div class="mockup-card-title">
          <span class="material-icons-outlined">storage</span>
          <span>Локальная база данных (Справочники)</span>
        </div>
        <div class="mockup-row">
          <div class="mockup-card mockup-flex-1" style="border-style:solid; padding:10px;">
            <span style="font-size:0.75rem; font-weight:700; color:var(--text-secondary);">👷 СОТРУДНИКИ</span>
            <div id="mockupWorkersList" style="display:flex; flex-direction:column; gap:4px; margin-top:6px;">
              <div class="mockup-item" style="padding:4px 8px;"><span>Иванов И.И. (Монтажник, 5 разряд)</span></div>
              <div class="mockup-item" style="padding:4px 8px;"><span>Петров П.П. (Машинист, 6 разряд)</span></div>
            </div>
            <button class="mockup-btn" id="mockupAddWorkerBtn" style="margin-top:6px; font-size:0.65rem; padding:4px 8px;">+ Добавить в базу</button>
          </div>
          <div class="mockup-card mockup-flex-1" style="border-style:solid; padding:10px;">
            <span style="font-size:0.75rem; font-weight:700; color:var(--text-secondary);">🚜 ТЕХНИКА</span>
            <div style="display:flex; flex-direction:column; gap:4px; margin-top:6px;">
              <div class="mockup-item" style="padding:4px 8px;"><span>Экскаватор CAT (Маш: Петров)</span></div>
              <div class="mockup-item" style="padding:4px 8px;"><span>Бульдозер Shantui (Маш: Сидоров)</span></div>
            </div>
          </div>
        </div>
      </div>
    `,
    excel: `
      <div class="mockup-screen-layout animate-fade-in" style="justify-content:center; align-items:center;">
        <span class="material-icons-outlined" style="font-size:4rem; color:var(--success); margin-bottom:12px;">table_view</span>
        <h3 style="font-size:1.1rem; margin-bottom:6px;">Генератор Excel-отчетов</h3>
        <p style="font-size:0.8rem; color:var(--text-secondary); text-align:center; max-width:320px; margin-bottom:16px;">
          Автоматическое форматирование шапки, группировка ресурсов, подсчет трудозатрат по формулам.
        </p>
        <button class="btn btn-primary btn-sm" id="mockupExportBtn">
          <span class="material-icons-outlined">download</span> Скачать Otchet_02_06.xlsx
        </button>
        <div id="mockupExportProgress" style="display:none; width:80%; height:4px; background:rgba(255,255,255,0.05); border-radius:2px; overflow:hidden; margin-top:12px;">
          <div id="mockupProgressBar" style="width:0%; height:100%; background:var(--success); transition:width 1s linear;"></div>
        </div>
        <span id="mockupExportSuccess" style="display:none; color:var(--success); font-size:0.8rem; margin-top:10px; font-weight:600;">Файл успешно сформирован!</span>
      </div>
    `
  };

  // Инициализация первого таба
  function setFeature(featureName) {
    mockupScreen.innerHTML = mockupTemplates[featureName];
    
    // Привязка скриптов для интерактивности внутри макета
    if (featureName === "timers") {
      initMockupTimers();
    } else if (featureName === "db") {
      initMockupDb();
    } else if (featureName === "excel") {
      initMockupExcel();
    }
  }

  setFeature("timers");

  showcaseTabs.forEach(tab => {
    tab.addEventListener("click", () => {
      showcaseTabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");
      const feat = tab.dataset.feature;
      setFeature(feat);
    });
  });

  // Логика таймеров внутри макета
  let mockupInterval;
  function initMockupTimers() {
    if (mockupInterval) clearInterval(mockupInterval);
    let seconds = 343; // 5 минут 43 секунды
    const timerEl = document.getElementById("mockupTickingTimer");
    const stopBtn = document.getElementById("mockupStopBtn");
    const splitBtn = document.getElementById("mockupSplitBtn");
    
    mockupInterval = setInterval(() => {
      seconds++;
      let h = Math.floor(seconds / 3600);
      let m = Math.floor((seconds % 3600) / 60);
      let s = seconds % 60;
      if (timerEl) {
        timerEl.textContent = `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
      }
    }, 1000);

    if (stopBtn) {
      stopBtn.addEventListener("click", () => {
        clearInterval(mockupInterval);
        if (timerEl) {
          timerEl.style.color = "var(--text-secondary)";
        }
        alert("Эмуляция остановки таймера. В приложении в этот момент фиксируется конечное время операции.");
      });
    }

    if (splitBtn) {
      splitBtn.addEventListener("click", () => {
        alert("Операция разделена! В приложении одна операция останавливается текущим временем, и мгновенно запускается следующая с тем же набором ресурсов.");
      });
    }
  }

  // Логика базы данных внутри макета
  function initMockupDb() {
    const addWorkerBtn = document.getElementById("mockupAddWorkerBtn");
    const list = document.getElementById("mockupWorkersList");
    
    if (addWorkerBtn && list) {
      addWorkerBtn.addEventListener("click", () => {
        const names = ["Козлов К.К. (Монтажник, 4 разряд)", "Смирнов С.С. (Мастер, 5 разряд)", "Васильев В.В. (Электрик, 6 разряд)"];
        const randomName = names[Math.floor(Math.random() * names.length)];
        
        // Добавляем запись
        const item = document.createElement("div");
        item.className = "mockup-item animate-fade-in";
        item.style.padding = "4px 8px";
        item.innerHTML = `<span>${randomName}</span>`;
        list.appendChild(item);
        
        // Лимитируем количество для аккуратности UI
        if (list.children.length > 4) {
          list.removeChild(list.firstElementChild);
        }
      });
    }
  }

  // Логика экспорта Excel внутри макета
  function initMockupExcel() {
    const exportBtn = document.getElementById("mockupExportBtn");
    const progress = document.getElementById("mockupExportProgress");
    const bar = document.getElementById("mockupProgressBar");
    const success = document.getElementById("mockupExportSuccess");

    if (exportBtn) {
      exportBtn.addEventListener("click", () => {
        exportBtn.style.display = "none";
        progress.style.display = "block";
        bar.style.width = "100%";

        setTimeout(() => {
          progress.style.display = "none";
          success.style.display = "block";
        }, 1200);
      });
    }
  }

  // --- ГЛАВНЫЙ ГЕРОЙ-ТАЙМЕР (Hero Ticking Timer) ---
  const heroTimer = document.querySelector(".ticking-timer");
  if (heroTimer) {
    let heroSecs = 15135; // 04:12:15
    setInterval(() => {
      heroSecs++;
      let h = Math.floor(heroSecs / 3600);
      let m = Math.floor((heroSecs % 3600) / 60);
      let s = heroSecs % 60;
      heroTimer.textContent = `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
    }, 1000);
  }

  // --- ЖИВОЙ СИМУЛЯТОР (Interactive Live Demo) ---
  const demoAddBtn = document.getElementById("demoAddBtn");
  const demoListContainer = document.getElementById("demoListContainer");
  const demoEmptyState = document.getElementById("demoEmptyState");
  const demoOpsCount = document.getElementById("demoOpsCount");
  const demoTotalTime = document.getElementById("demoTotalTime");

  let demoOperations = [];
  let demoStatsInterval;

  function updateDemoStats() {
    demoOpsCount.textContent = demoOperations.length;
    
    // Считаем общее время
    let totalSecs = 0;
    demoOperations.forEach(op => {
      if (op.active) {
        totalSecs += Math.floor((Date.now() - op.startEpoch) / 1000);
      } else {
        totalSecs += op.duration;
      }
    });

    let h = Math.floor(totalSecs / 3600);
    let m = Math.floor((totalSecs % 3600) / 60);
    let s = totalSecs % 60;
    demoTotalTime.textContent = `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
  }

  if (demoAddBtn) {
    demoAddBtn.addEventListener("click", () => {
      // Скрываем пустой холст
      if (demoEmptyState) demoEmptyState.style.display = "none";
      
      const names = [
        "Установка ЖБ лотков колодца",
        "Бетонирование фундамента опоры",
        "Сварка стыков трубопровода d325",
        "Планировка земляного полотна",
        "Гидроизоляция стыков плит"
      ];
      
      const randomName = names[Math.floor(Math.random() * names.length)];
      const opId = Date.now();
      
      const newOp = {
        id: opId,
        name: randomName,
        startEpoch: Date.now(),
        duration: 0,
        active: true
      };

      demoOperations.push(newOp);
      
      // Рендерим элемент
      const div = document.createElement("div");
      div.className = "demo-op-item";
      div.dataset.id = opId;
      div.innerHTML = `
        <div class="demo-op-details">
          <span class="demo-op-name">${randomName}</span>
          <span class="demo-op-time">Старт: ${new Date().toLocaleTimeString("ru-RU")}</span>
        </div>
        <div class="demo-op-actions">
          <span class="demo-timer-span ticking-demo-timer">00:00:00</span>
          <button class="demo-btn-stop" data-id="${opId}">
            <span class="material-icons-outlined" style="font-size:14px;">stop</span> Стоп
          </button>
        </div>
      `;

      // Привязка стоп-кнопки
      div.querySelector(".demo-btn-stop").addEventListener("click", (e) => {
        const id = Number(e.currentTarget.dataset.id);
        const op = demoOperations.find(o => o.id === id);
        if (op && op.active) {
          op.active = false;
          op.duration = Math.floor((Date.now() - op.startEpoch) / 1000);
          
          // Обновляем визуально
          const card = demoListContainer.querySelector(`.demo-op-item[data-id="${id}"]`);
          if (card) {
            card.style.borderLeft = "3px solid var(--text-muted)";
            const actions = card.querySelector(".demo-op-actions");
            
            let h = Math.floor(op.duration / 3600);
            let m = Math.floor((op.duration % 3600) / 60);
            let s = op.duration % 60;
            const formatted = `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
            
            actions.innerHTML = `
              <span class="demo-timer-span" style="color:var(--text-secondary);">${formatted}</span>
              <span class="material-icons-outlined" style="color:var(--text-muted); font-size:18px;">check_circle</span>
            `;
          }
          updateDemoStats();
        }
      });

      demoListContainer.insertBefore(div, demoListContainer.firstChild);
      updateDemoStats();
      
      // Запускаем общий таймер обновлений если не запущен
      if (!demoStatsInterval) {
        demoStatsInterval = setInterval(() => {
          // Обновляем бегущие циферблаты
          const activeTimers = demoListContainer.querySelectorAll(".ticking-demo-timer");
          activeTimers.forEach(timerEl => {
            const card = timerEl.closest(".demo-op-item");
            const id = Number(card.dataset.id);
            const op = demoOperations.find(o => o.id === id);
            if (op && op.active) {
              const diffSec = Math.floor((Date.now() - op.startEpoch) / 1000);
              let h = Math.floor(diffSec / 3600);
              let m = Math.floor((diffSec % 3600) / 60);
              let s = diffSec % 60;
              timerEl.textContent = `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
            }
          });
          
          updateDemoStats();
        }, 1000);
      }
    });
  }

  // --- АНИМАЦИИ ПРИ ПРОКРУТКЕ (Scroll Reveal) ---
  const revealElements = document.querySelectorAll(".showcase-tab, .platform-card, .demo-card, .section-header, .gallery-item");
  
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.style.opacity = "1";
        entry.target.style.transform = "translateY(0)";
        observer.unobserve(entry.target);
      }
    });
  }, {
    threshold: 0.1
  });

  revealElements.forEach(el => {
    el.style.opacity = "0";
    el.style.transform = "translateY(30px)";
    el.style.transition = "transform 0.8s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.8s ease";
    observer.observe(el);
  });
});
