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
  let mockupInterval;

  const mockupTemplates = {
    timers: `
      <div class="mockup-screen-layout animate-fade-in">
        <div class="mockup-card-title">
          <span class="material-icons-outlined">timer</span>
          <span>Секундомеры реального времени</span>
        </div>
        <div class="mockup-timers-list" style="display:flex; flex-direction:column; gap:10px; overflow-y:auto; height: 260px; padding-right: 4px;">
          <div class="mockup-card" id="mockupActiveCard" style="border-left: 3px solid var(--success);">
            <div class="mockup-item">
              <span style="font-weight:600;" id="mockupActiveTitle">Укладка асфальтобетона</span>
              <span class="demo-timer-span" id="mockupTickingTimer" style="color:var(--success); font-family:monospace; font-weight:700;">00:05:43</span>
            </div>
            <p style="font-size:0.7rem; color:var(--text-secondary); margin-top:2px;">Ресурсы: Асфальтоукладчик • 👥 4 человека</p>
            <div style="display:flex; gap:8px; margin-top:8px;">
              <button class="mockup-btn" id="mockupSplitBtn">Разделить</button>
              <button class="mockup-btn" style="background:var(--danger);" id="mockupStopBtn">Стоп</button>
            </div>
          </div>
          <div class="mockup-card" style="border-style:solid; border-color:rgba(255,255,255,0.05); background:rgba(0,0,0,0.15); border-left: 3px solid var(--text-muted);">
            <div class="mockup-item" style="color:var(--text-secondary);">
              <span>Выемка грунта в отвал</span>
              <span style="font-weight:600;">01:45:00</span>
            </div>
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

  // Логика таймеров внутри макета
  function initMockupTimers() {
    if (mockupInterval) clearInterval(mockupInterval);
    let activeSeconds = 343; // 5 минут 43 секунды
    
    const bindMockupEvents = () => {
      const timerEl = document.getElementById("mockupTickingTimer");
      const stopBtn = document.getElementById("mockupStopBtn");
      const splitBtn = document.getElementById("mockupSplitBtn");
      const timersList = document.querySelector(".mockup-timers-list");

      if (stopBtn) {
        stopBtn.onclick = () => {
          clearInterval(mockupInterval);
          if (timerEl) {
            timerEl.style.color = "var(--text-secondary)";
          }
          const btnContainer = stopBtn.parentElement;
          if (btnContainer) {
            btnContainer.innerHTML = '<span style="color:var(--text-secondary); font-size:0.75rem; font-weight:600;">Завершен</span>';
          }
        };
      }

      if (splitBtn) {
        splitBtn.onclick = () => {
          clearInterval(mockupInterval);
          
          let h = Math.floor(activeSeconds / 3600);
          let m = Math.floor((activeSeconds % 3600) / 60);
          let s = activeSeconds % 60;
          const formattedPrev = `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;

          const activeCard = document.getElementById("mockupActiveCard");
          if (activeCard) {
            activeCard.id = "";
            activeCard.style.borderLeft = "3px solid var(--text-muted)";
            activeCard.style.background = "rgba(0,0,0,0.15)";
            activeCard.style.borderColor = "rgba(255,255,255,0.05)";
            activeCard.style.borderStyle = "solid";
            
            const activeTitle = document.getElementById("mockupActiveTitle")?.textContent || "Укладка асфальтобетона";
            activeCard.innerHTML = `
              <div class="mockup-item" style="color:var(--text-secondary);">
                <span>${activeTitle} (Часть 1)</span>
                <span style="font-weight:600;">${formattedPrev}</span>
              </div>
            `;
          }

          const newActiveCard = document.createElement("div");
          newActiveCard.className = "mockup-card animate-fade-in";
          newActiveCard.id = "mockupActiveCard";
          newActiveCard.style.borderLeft = "3px solid var(--success)";
          newActiveCard.innerHTML = `
            <div class="mockup-item">
              <span style="font-weight:600;" id="mockupActiveTitle">Укладка асфальтобетона (Часть 2)</span>
              <span class="demo-timer-span" id="mockupTickingTimer" style="color:var(--success); font-family:monospace; font-weight:700;">00:00:00</span>
            </div>
            <p style="font-size:0.7rem; color:var(--text-secondary); margin-top:2px;">Ресурсы: Асфальтоукладчик • 👥 4 человека</p>
            <div style="display:flex; gap:8px; margin-top:8px;">
              <button class="mockup-btn" id="mockupSplitBtn">Разделить</button>
              <button class="mockup-btn" style="background:var(--danger);" id="mockupStopBtn">Стоп</button>
            </div>
          `;
          
          if (timersList) {
            timersList.insertBefore(newActiveCard, timersList.firstChild);
          }

          activeSeconds = 0;
          mockupInterval = setInterval(() => {
            activeSeconds++;
            let h2 = Math.floor(activeSeconds / 3600);
            let m2 = Math.floor((activeSeconds % 3600) / 60);
            let s2 = activeSeconds % 60;
            const timerEl2 = document.getElementById("mockupTickingTimer");
            if (timerEl2) {
              timerEl2.textContent = `${String(h2).padStart(2, "0")}:${String(m2).padStart(2, "0")}:${String(s2).padStart(2, "0")}`;
            }
          }, 1000);

          bindMockupEvents();
        };
      }
    };

    mockupInterval = setInterval(() => {
      activeSeconds++;
      let h = Math.floor(activeSeconds / 3600);
      let m = Math.floor((activeSeconds % 3600) / 60);
      let s = activeSeconds % 60;
      const timerEl = document.getElementById("mockupTickingTimer");
      if (timerEl) {
        timerEl.textContent = `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
      }
    }, 1000);

    bindMockupEvents();
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

  // Инициализация первого таба
  setFeature("timers");

  showcaseTabs.forEach(tab => {
    tab.addEventListener("click", () => {
      showcaseTabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");
      const feat = tab.dataset.feature;
      setFeature(feat);
    });
  });

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

  function formatTimeSpan(totalSecs) {
    let h = Math.floor(totalSecs / 3600);
    let m = Math.floor((totalSecs % 3600) / 60);
    let s = totalSecs % 60;
    return `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
  }

  function updateDemoStats() {
    demoOpsCount.textContent = demoOperations.length;
    
    let totalSecs = 0;
    demoOperations.forEach(op => {
      if (op.active) {
        totalSecs += Math.floor((Date.now() - op.startEpoch) / 1000);
      } else {
        totalSecs += op.duration;
      }
    });

    demoTotalTime.textContent = formatTimeSpan(totalSecs);
  }

  const createOperationCard = (randomName, opId, startEpoch, active = true, duration = 0, initialNote = "") => {
    const newOp = {
      id: opId,
      name: randomName,
      startEpoch: startEpoch,
      duration: duration,
      active: active,
      note: initialNote
    };

    demoOperations.push(newOp);
    
    const div = document.createElement("div");
    div.className = "demo-op-item";
    div.dataset.id = opId;
    
    if (!active) {
      div.style.borderLeft = "3px solid var(--text-muted)";
    } else {
      div.style.borderLeft = "3px solid var(--success)";
    }

    div.innerHTML = `
      <div class="demo-op-details" style="flex:1;">
        <span class="demo-op-name" style="font-weight: 600; font-size: 0.85rem; display: block;">${randomName}</span>
        <span class="demo-op-time" style="font-size: 0.75rem; color: var(--text-secondary);">Старт: ${new Date(startEpoch).toLocaleTimeString("ru-RU")}</span>
        <span class="demo-op-note" style="font-size: 0.75rem; color: #3388ff; margin-top: 4px; display: ${initialNote ? 'block' : 'none'};">
          ${initialNote ? '📝 Заметка: ' + initialNote : ''}
        </span>
      </div>
      <div class="demo-op-actions" style="display: flex; gap: 8px; align-items: center;">
        ${active ? `
          <span class="demo-timer-span ticking-demo-timer">00:00:00</span>
          <button class="demo-btn-action demo-btn-note" data-id="${opId}" title="Добавить заметку" style="background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.08); color: var(--text-secondary); border-radius: 6px; padding: 4px 8px; font-size: 0.75rem; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: var(--transition-fast);">
            <span class="material-icons-outlined" style="font-size:14px;">chat_bubble_outline</span>
          </button>
          <button class="demo-btn-action demo-btn-split" data-id="${opId}" title="Разделить операцию" style="background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.08); color: var(--text-secondary); border-radius: 6px; padding: 4px 8px; font-size: 0.75rem; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: var(--transition-fast);">
            <span class="material-icons-outlined" style="font-size:14px;">call_split</span>
          </button>
          <button class="demo-btn-stop" data-id="${opId}">
            <span class="material-icons-outlined" style="font-size:14px;">stop</span> Стоп
          </button>
        ` : `
          <span class="demo-timer-span" style="color:var(--text-secondary);">${formatTimeSpan(duration)}</span>
          <span class="material-icons-outlined" style="color:var(--text-muted); font-size:18px;">check_circle</span>
        `}
      </div>
    `;

    // Эмуляция наведения мыши для кнопок
    const actionBtns = div.querySelectorAll(".demo-btn-action");
    actionBtns.forEach(btn => {
      btn.onmouseover = () => {
        btn.style.background = "var(--primary-glow)";
        btn.style.borderColor = "var(--primary)";
        btn.style.color = "var(--text-primary)";
      };
      btn.onmouseout = () => {
        btn.style.background = "rgba(255, 255, 255, 0.05)";
        btn.style.borderColor = "rgba(255, 255, 255, 0.08)";
        btn.style.color = "var(--text-secondary)";
      };
    });

    // Обработка кнопки добавления заметки
    const noteBtn = div.querySelector(".demo-btn-note");
    if (noteBtn) {
      noteBtn.addEventListener("click", () => {
        const noteText = prompt("Введите текст заметки для операции:");
        if (noteText && noteText.trim() !== "") {
          const op = demoOperations.find(o => o.id === opId);
          if (op) {
            op.note = noteText.trim();
            const noteSpan = div.querySelector(".demo-op-note");
            if (noteSpan) {
              noteSpan.textContent = `📝 Заметка: ${op.note}`;
              noteSpan.style.display = "block";
            }
          }
        }
      });
    }

    // Обработка кнопки разделения
    const splitBtn = div.querySelector(".demo-btn-split");
    if (splitBtn) {
      splitBtn.addEventListener("click", () => {
        const op = demoOperations.find(o => o.id === opId);
        if (op && op.active) {
          // Останавливаем текущую операцию
          op.active = false;
          op.duration = Math.floor((Date.now() - op.startEpoch) / 1000);

          // Обновляем текущую карточку
          div.style.borderLeft = "3px solid var(--text-muted)";
          const actions = div.querySelector(".demo-op-actions");
          if (actions) {
            actions.innerHTML = `
              <span class="demo-timer-span" style="color:var(--text-secondary);">${formatTimeSpan(op.duration)}</span>
              <span class="material-icons-outlined" style="color:var(--text-muted); font-size:18px;">check_circle</span>
            `;
          }

          // Определяем новое имя (Часть N)
          let splitName;
          const match = op.name.match(/\(Часть (\d+)\)/);
          if (match) {
            const nextPart = parseInt(match[1]) + 1;
            splitName = op.name.replace(/\(Часть \d+\)/, `(Часть ${nextPart})`);
          } else {
            splitName = `${op.name} (Часть 2)`;
            const nameSpan = div.querySelector(".demo-op-name");
            if (nameSpan) {
              nameSpan.textContent = `${op.name} (Часть 1)`;
            }
            op.name = `${op.name} (Часть 1)`;
          }

          // Создаем новую операцию
          createOperationCard(splitName, Date.now(), Date.now(), true, 0, op.note);
        }
      });
    }

    // Обработка кнопки «Стоп»
    const stopBtn = div.querySelector(".demo-btn-stop");
    if (stopBtn) {
      stopBtn.addEventListener("click", () => {
        const op = demoOperations.find(o => o.id === opId);
        if (op && op.active) {
          op.active = false;
          op.duration = Math.floor((Date.now() - op.startEpoch) / 1000);

          div.style.borderLeft = "3px solid var(--text-muted)";
          const actions = div.querySelector(".demo-op-actions");
          if (actions) {
            actions.innerHTML = `
              <span class="demo-timer-span" style="color:var(--text-secondary);">${formatTimeSpan(op.duration)}</span>
              <span class="material-icons-outlined" style="color:var(--text-muted); font-size:18px;">check_circle</span>
            `;
          }
          updateDemoStats();
        }
      });
    }

    demoListContainer.insertBefore(div, demoListContainer.firstChild);
    updateDemoStats();
  };

  if (demoAddBtn) {
    demoAddBtn.addEventListener("click", () => {
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
      
      createOperationCard(randomName, opId, Date.now(), true, 0, "");

      if (!demoStatsInterval) {
        demoStatsInterval = setInterval(() => {
          const activeTimers = demoListContainer.querySelectorAll(".ticking-demo-timer");
          activeTimers.forEach(timerEl => {
            const card = timerEl.closest(".demo-op-item");
            const id = Number(card.dataset.id);
            const op = demoOperations.find(o => o.id === id);
            if (op && op.active) {
              const diffSec = Math.floor((Date.now() - op.startEpoch) / 1000);
              timerEl.textContent = formatTimeSpan(diffSec);
            }
          });
          updateDemoStats();
        }, 1000);
      }
    });
  }

  // --- ПРЕДУПРЕЖДЕНИЕ ПЕРЕД ПЕРЕХОДОМ К PWA ---
  const pwaLinks = document.querySelectorAll(".pwa-link");
  pwaLinks.forEach(link => {
    link.addEventListener("click", (e) => {
      const confirmTransition = confirm(
        "Внимание!\n\nПеред использованием PWA-версии на iPhone обязательно ознакомьтесь с инструкцией по установке и правилами защиты данных от автоматического удаления (находятся внизу страницы).\n\nВы уже прочитали инструкцию и хотите перейти к приложению?"
      );
      if (!confirmTransition) {
        e.preventDefault();
        const warningCard = document.querySelector(".ios-instructions-card");
        if (warningCard) {
          warningCard.scrollIntoView({ behavior: "smooth" });
          warningCard.style.boxShadow = "0 0 35px rgba(239, 68, 68, 0.45)";
          setTimeout(() => {
            warningCard.style.boxShadow = "";
          }, 2500);
        }
      }
    });
  });

  // --- АНИМАЦИИ ПРИ ПРОКРУТКЕ (Scroll Reveal) ---
  const revealElements = document.querySelectorAll(".showcase-tab, .platform-card, .demo-card, .section-header, .comparison-card");
  
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
