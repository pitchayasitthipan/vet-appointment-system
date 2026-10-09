"use strict";
(() => {
  const P = window.PawAppointments, el = id => document.getElementById(id);
  let owner = P.owner(), page = 0, totalPages = 0, loadVersion = 0, editVersion = 0, selected = null, cancelling = null, saving = false;
  const editDialog = el("edit-dialog"), cancelDialog = el("cancel-dialog");
  function text(tag, value, className) { const node = document.createElement(tag); node.textContent = value; if (className) node.className = className; return node; }
  function scoped(id) { return `/api/appointments/${id}?${P.query({ownerId:owner.ownerId})}`; }
  function canChange(a) { return ["PENDING","CONFIRMED"].includes(a.status) && new Date(a.appointmentDateTime + "+07:00") > new Date(); }
  function render(rows) {
    const container = el("appointment-list"); container.replaceChildren();
    if (!rows.length) { container.append(text("p", "ยังไม่มีนัดหมายตามเงื่อนไขนี้", "card empty-state")); return; }
    rows.forEach(a => {
      const card = text("article", "", "card appointment-card");
      const header = text("div", "", "appointment-card-header");
      header.append(text("h2", `${a.petName} · นัด #${a.appointmentId}`), text("span", P.statuses[a.status] || a.status, `status-badge ${a.status.toLowerCase()}`));
      card.append(header, text("p", P.formatDate(a.appointmentDateTime), "appointment-date"),
        text("p", `${a.doctorName} · ${P.services[a.serviceType] || a.serviceType}`), text("p", a.symptoms), text("p", a.preparationInstructions, "field-hint"));
      if (canChange(a)) {
        const actions = text("div", "", "appointment-actions");
        const edit = text("button", "แก้ไข / เลื่อนนัด", "secondary"); edit.type = "button"; edit.addEventListener("click", () => openEdit(a.appointmentId));
        const cancel = text("button", "ยกเลิกนัด", "danger"); cancel.type = "button"; cancel.addEventListener("click", () => {
          cancelling = a; el("cancel-description").textContent = `นัด #${a.appointmentId} ของ ${a.petName} · ${P.formatDate(a.appointmentDateTime)}`;
          P.message(el("cancel-message"), ""); cancelDialog.showModal();
        }); actions.append(edit,cancel); card.append(actions);
      }
      container.append(card);
    });
  }
  async function load() {
    if (!owner) return;
    const version = ++loadVersion;
    el("list-section").hidden = false; el("selected-owner").textContent = owner.firstName ? `แฟ้ม ${owner.firstName} ${owner.lastName}` : "แฟ้มที่คุณเลือก";
    el("previous-page").disabled = true; el("next-page").disabled = true;
    P.message(el("list-message"), "กำลังโหลดนัดหมาย…");
    try {
      const result = await P.request(`/api/appointments?${P.query({ownerId:owner.ownerId,page,size:10,status:el("status-filter").value,direction:el("sort-direction").value})}`);
      if (version !== loadVersion) return;
      totalPages = result.totalPages;
      if (page > 0 && page >= totalPages) { page = Math.max(0,totalPages-1); return load(); }
      render(result.content); P.message(el("list-message"), "");
      el("page-info").textContent = `หน้า ${totalPages ? page+1 : 0} / ${totalPages} · ${result.totalElements} นัด`;
      el("previous-page").disabled = page === 0; el("next-page").disabled = page+1 >= totalPages;
    } catch (e) { if (version === loadVersion) { el("appointment-list").replaceChildren(); P.message(el("list-message"), e.message, "error"); } }
  }
  function clear() {
    loadVersion++; editVersion++; owner = null; page = 0; selected = null; cancelling = null; P.clearOwner();
    el("list-section").hidden = true; el("appointment-list").replaceChildren(); el("selected-owner").textContent = "";
    if (editDialog.open) editDialog.close(); if (cancelDialog.open) cancelDialog.close();
  }
  el("list-phone").addEventListener("input", () => { if (!saving) clear(); });
  el("list-lookup-form").addEventListener("submit", async event => {
    event.preventDefault(); clear(); const version = loadVersion;
    const button = event.currentTarget.querySelector("button"); button.disabled = true;
    try { const found = await P.lookup(el("list-phone").value.trim()); if (version !== loadVersion) return; owner = found; P.setOwner(found); await load(); }
    catch(e) { if (version === loadVersion) P.message(el("list-message"), e.status === 404 ? "ไม่พบแฟ้มจากเบอร์นี้ กรุณาลงทะเบียนผ่านหน้าจองนัดใหม่" : e.message, "error"); }
    finally { button.disabled = false; }
  });
  ["status-filter","sort-direction"].forEach(id => el(id).addEventListener("change", () => {page=0;load();}));
  el("refresh-list").addEventListener("click", load);
  el("previous-page").addEventListener("click", () => {if(page>0){page--;load();}});
  el("next-page").addEventListener("click", () => {if(page+1<totalPages){page++;load();}});
  async function editSlots() {
    const version = ++editVersion;
    P.options(el("edit-time"), [], "กำลังตรวจคิว…"); el("save-edit").disabled = true;
    if (!selected || !el("edit-doctor").value || !el("edit-date").value) return;
    try {
      const slots = await P.request(`/api/appointments/availability?${P.query({doctorId:el("edit-doctor").value,date:el("edit-date").value})}`);
      if(version!==editVersion || !selected) return;
      const own = selected.appointmentDateTime;
      if(String(selected.doctorId)===el("edit-doctor").value && own.slice(0,10)===el("edit-date").value && !slots.includes(own)) slots.push(own);
      slots.sort(); P.options(el("edit-time"),slots.map(slot=>[slot,slot.slice(11,16)+" น."]),slots.length?"เลือกเวลา":"ไม่มีคิวว่าง");
      if(slots.includes(own)) el("edit-time").value=own;
      P.message(el("edit-message"),slots.length?"":"ไม่มีคิวว่าง กรุณาเลือกวันหรือสัตวแพทย์อื่น");
    } catch(e){if(version===editVersion)P.message(el("edit-message"),e.message,"error");}
    el("save-edit").disabled = saving || !el("edit-time").value;
  }
  async function openEdit(id) {
    if(saving || !owner) return;
    const version=++editVersion;
    try {
      const [a,doctors]=await Promise.all([P.request(scoped(id)),P.request("/api/doctors")]);
      if(version!==editVersion || !owner) return;
      selected=a; el("edit-pet").textContent=`${a.petName} · นัด #${a.appointmentId}`;
      P.options(el("edit-doctor"),doctors.map(d=>[d.doctorId,`${d.firstName} ${d.lastName}`]),"เลือกสัตวแพทย์");
      el("edit-doctor").value=String(a.doctorId); el("edit-date").min=P.bangkokToday(); el("edit-date").value=a.appointmentDateTime.slice(0,10);
      el("edit-service").value=a.serviceType; el("edit-symptoms").value=a.symptoms; P.message(el("edit-message"),"");
      editDialog.showModal(); await editSlots();
    } catch(e){P.message(el("list-message"),e.message,"error");}
  }
  ["edit-doctor","edit-date"].forEach(id=>el(id).addEventListener("change",editSlots));
  el("edit-time").addEventListener("change",()=>{el("save-edit").disabled=saving||!el("edit-time").value;});
  el("close-edit").addEventListener("click",()=>{if(!saving){editVersion++;selected=null;editDialog.close();}});
  el("close-cancel").addEventListener("click",()=>{if(!saving){cancelling=null;cancelDialog.close();}});
  function busy(value) {
    saving=value; el("list-lookup-form").querySelector("fieldset").disabled=value;
    el("edit-fields").disabled=value; el("save-edit").disabled=value||!el("edit-time").value;
    el("confirm-cancel").disabled=value; el("close-edit").disabled=value; el("close-cancel").disabled=value;
  }
  editDialog.addEventListener("cancel",event=>{if(saving)event.preventDefault();else{editVersion++;selected=null;}});
  cancelDialog.addEventListener("cancel",event=>{if(saving)event.preventDefault();else cancelling=null;});
  el("edit-form").addEventListener("submit",async event=>{
    event.preventDefault(); if(saving||!selected||!owner||!event.currentTarget.reportValidity())return;
    busy(true);
    try {
      await P.request(scoped(selected.appointmentId),{method:"PUT",body:JSON.stringify({doctorId:Number(el("edit-doctor").value),
        appointmentDateTime:el("edit-time").value,serviceType:el("edit-service").value,symptoms:el("edit-symptoms").value.trim(),version:selected.version})});
      selected=null; editDialog.close(); await load(); P.message(el("list-message"),"บันทึกการแก้ไขนัดหมายแล้ว","success");
    } catch(e){P.message(el("edit-message"),e.message+(e.status===409?" · ปิดหน้าต่างแล้วเปิดแก้ไขใหม่เพื่อโหลดข้อมูลล่าสุด":""),"error");}
    finally{busy(false);}
  });
  el("confirm-cancel").addEventListener("click",async()=>{
    if(saving||!cancelling||!owner)return; busy(true);
    try {await P.request(`/api/appointments/${cancelling.appointmentId}/cancel?${P.query({ownerId:owner.ownerId})}`,{method:"PATCH"});
      cancelling=null;cancelDialog.close();await load();P.message(el("list-message"),"ยกเลิกนัดหมายแล้ว","success");}
    catch(e){P.message(el("cancel-message"),e.message,"error");}finally{busy(false);}
  });
  if(owner) load();
})();
