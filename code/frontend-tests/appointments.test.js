import { test } from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { JSDOM, VirtualConsole } from "jsdom";

const root = new URL("../src/main/resources/static/", import.meta.url);
const owner = { ownerId: 1, firstName: "อ้น", lastName: "ทดสอบ" };
const slot = "2099-01-05T09:00:00";
const appointment = { appointmentId: 4, ownerId: 1, petId: 2, petName: "<script>bad()</script>", doctorId: 3,
  doctorName: "หมอ ใจดี", appointmentDateTime: slot, status: "PENDING", serviceType: "VACCINE", symptoms: "วัคซีน", preparationInstructions: "นำสมุดวัคซีน", version: 0 };
const doctors = [{doctorId:3,firstName:"หมอ",lastName:"ใจดี"}];

async function until(predicate) {
  for (let i = 0; i < 200; i++) { if (predicate()) return; await new Promise(r => setTimeout(r, 5)); }
  assert.fail("UI did not reach the expected state");
}
function setup(page, script, handler, cached = false) {
  const dom = new JSDOM(readFileSync(new URL(page, root), "utf8"), {url:"http://localhost/" + page,
    runScripts:"outside-only", virtualConsole:new VirtualConsole()});
  const w = dom.window;
  w.HTMLDialogElement.prototype.showModal = function() {this.open=true;};
  w.HTMLDialogElement.prototype.close = function() {this.open=false;};
  if(cached) w.sessionStorage.setItem("pawcare.owner",JSON.stringify(owner));
  const calls=[];
  w.fetch=async(url,options={})=>{calls.push({url,options}); const result=await handler(url,options);
    return {ok:(result.status||200)<400,status:result.status||200,json:async()=>result.body};};
  w.eval(readFileSync(new URL("js/appointment-common.js",root),"utf8"));
  w.eval(readFileSync(new URL("js/"+script,root),"utf8"));
  const el=id=>w.document.getElementById(id);
  const change=(id,value)=>{el(id).value=value;el(id).dispatchEvent(new w.Event("change",{bubbles:true}));};
  const submit=id=>el(id).dispatchEvent(new w.Event("submit",{bubbles:true,cancelable:true}));
  return {dom,w,el,change,submit,calls};
}
const bookHandler=(url,options)=>{
  if(url==="/api/doctors")return {body:doctors};
  if(url.endsWith("/config"))return {body:{ownerRegistrationPath:"/owners/new",ownerRegistrationEnabled:"false",petRegistrationEnabled:"false"}};
  if(url.endsWith("/lookup"))return {body:owner};
  if(url.endsWith("/pets"))return {body:[{petId:2,petName:"มะลิ"}]};
  if(url.includes("/availability?"))return {body:[slot]};
  if(options.method==="POST"&&url==="/api/v1/appointments")return {status:201,body:appointment};
  throw new Error("Unexpected API: "+url);
};
async function chooseBooking(ui) {
  await until(()=>ui.el("doctor-id").options.length===2);
  ui.el("owner-phone").value="0812345678";ui.submit("owner-lookup-form");
  await until(()=>!ui.el("pet-fields").disabled);
  ui.change("pet-id","2");ui.change("doctor-id","3");ui.change("appointment-date",slot.slice(0,10));
  await until(()=>ui.el("appointment-time").options.length===2);
  ui.change("appointment-time",slot);ui.w.document.querySelector('[name="serviceType"][value="VACCINE"]').checked=true;
  ui.el("symptoms").value="วัคซีน";
}

test("booking submits selected owner/pet/slot and shows success only after API save",async()=>{
  const ui=setup("appointment-create.html","appointment-create.js",bookHandler);
  try {
    await chooseBooking(ui);assert.equal(ui.el("submit-appointment").disabled,false);ui.submit("appointment-form");
    await until(()=>!ui.el("booking-result-links").hidden);
    const posted=ui.calls.find(c=>c.url==="/api/v1/appointments");
    assert.deepEqual(JSON.parse(posted.options.body),{ownerId:1,petId:2,doctorId:3,appointmentDateTime:slot,serviceType:"VACCINE",symptoms:"วัคซีน"});
    assert.match(ui.el("booking-message").textContent,/บันทึกนัด #4/);
    assert.equal(ui.el("submit-appointment").disabled,true);
  } finally{ui.dom.window.close();}
});
test("conflict refreshes slots and never reports booking success",async()=>{
  const ui=setup("appointment-create.html","appointment-create.js",(url,options)=>options.method==="POST"&&url==="/api/v1/appointments"
    ? {status:409,body:{message:"คิวถูกจองแล้ว"}} : bookHandler(url,options));
  try{await chooseBooking(ui);ui.submit("appointment-form");await until(()=>ui.el("booking-message").textContent.includes("คิวถูกจองแล้ว"));
    assert.equal(ui.el("booking-result-links").hidden,true);
    assert.ok(ui.calls.filter(c=>c.url.includes("availability")).length>=2);
  }finally{ui.dom.window.close();}
});
test("changing phone invalidates selected owner and late lookup response",async()=>{
  let resolveLookup;
  const ui=setup("appointment-create.html","appointment-create.js",(url,options)=>url.endsWith("/lookup")
    ? new Promise(resolve=>{resolveLookup=resolve;}) : bookHandler(url,options));
  try{ui.el("owner-phone").value="0812345678";ui.submit("owner-lookup-form");await until(()=>resolveLookup);
    ui.el("owner-phone").value="0899999999";ui.el("owner-phone").dispatchEvent(new ui.w.Event("input"));
    resolveLookup({body:owner});await until(()=>!ui.el("owner-lookup-form").querySelector("button").disabled);
    assert.equal(ui.el("pet-fields").disabled,true);assert.equal(ui.w.PawAppointments.owner(),null);
  }finally{ui.dom.window.close();}
});
test("registration destination retains phone and appointment return flow",async()=>{
  const ui=setup("appointment-create.html","appointment-create.js",(url,options)=>url.endsWith("/config")
    ? {body:{ownerRegistrationPath:"/owners/new"}} : bookHandler(url,options));
  try{assert.equal(await ui.w.PawAppointments.registration("081-234-5678"),"/owners/new?phone=0812345678&returnTo=appointment");}
  finally{ui.dom.window.close();}
});
const listHandler=(url,options)=>{
  if(url==="/api/doctors")return {body:doctors};
  if(url.startsWith("/api/v1/appointments/availability"))return {body:["2099-01-05T09:30:00"]};
  if(url.startsWith("/api/v1/appointments?"))return {body:{content:[appointment],totalPages:1,totalElements:1}};
  if(url.startsWith("/api/v1/appointments/4"))return {body:appointment};
  throw new Error("Unexpected API: "+url);
};
test("list renders names as text, scopes owner, and saves current version",async()=>{
  const ui=setup("appointments.html","appointments.js",listHandler,true);
  try{await until(()=>ui.el("appointment-list").querySelector("button"));
    assert.equal(ui.el("appointment-list").querySelector("script"),null);
    assert.ok(ui.calls[0].url.includes("ownerId=1"));
    ui.el("appointment-list").querySelector("button").click();
    await until(()=>ui.el("edit-dialog").open&&!ui.el("save-edit").disabled);
    assert.equal(ui.el("edit-time").value,slot); // existing occupied slot must remain selectable
    ui.el("edit-symptoms").value="ตรวจเพิ่ม";ui.submit("edit-form");
    await until(()=>ui.calls.some(c=>c.options.method==="PUT"));
    const update=ui.calls.find(c=>c.options.method==="PUT");assert.equal(JSON.parse(update.options.body).version,0);
    assert.ok(update.url.endsWith("ownerId=1"));
    await until(()=>!ui.el("edit-dialog").open);
  }finally{ui.dom.window.close();}
});
test("cancel waits for confirmation and sends owner-scoped PATCH",async()=>{
  const ui=setup("appointments.html","appointments.js",listHandler,true);
  try{await until(()=>ui.el("appointment-list").querySelector(".danger"));
    ui.el("appointment-list").querySelector(".danger").click();
    assert.equal(ui.el("cancel-dialog").open,true);assert.equal(ui.calls.some(c=>c.options.method==="PATCH"),false);
    ui.el("confirm-cancel").click();await until(()=>ui.calls.some(c=>c.options.method==="PATCH"));
    assert.equal(ui.calls.find(c=>c.options.method==="PATCH").url,"/api/v1/appointments/4/cancel?ownerId=1");
    await until(()=>!ui.el("cancel-dialog").open);
  }finally{ui.dom.window.close();}
});

test("owner without pets sees pending team integration and cannot submit booking",async()=>{
  const ui=setup("appointment-create.html","appointment-create.js",(url,options)=>url.endsWith("/pets")
    ? {body:[]} : bookHandler(url,options));
  try{ui.el("owner-phone").value="0812345678";ui.submit("owner-lookup-form");
    await until(()=>ui.el("booking-message").textContent.includes("รอเชื่อมหน้าเพิ่มสัตว์เลี้ยง"));
    assert.equal(ui.el("pet-fields").disabled,true);assert.equal(ui.el("submit-appointment").disabled,true);
    assert.equal(ui.el("registration-link-container").hidden,true);
  }finally{ui.dom.window.close();}
});

test("unknown phone stays on booking when owner registration is pending",async()=>{
  const ui=setup("appointment-create.html","appointment-create.js",(url,options)=>url.endsWith("/lookup")
    ? {status:404,body:{message:"ไม่พบเบอร์นี้"}} : bookHandler(url,options));
  try{ui.el("owner-phone").value="0812345678";ui.submit("owner-lookup-form");
    await until(()=>ui.el("owner-result").textContent.includes("รอเชื่อมหน้าลงทะเบียน"));
    assert.equal(ui.w.location.pathname,"/appointment-create.html");
    assert.equal(ui.el("submit-appointment").disabled,true);
  }finally{ui.dom.window.close();}
});

test("list filtering resets pagination and sends the selected status",async()=>{
  const ui=setup("appointments.html","appointments.js",(url,options)=>url.startsWith("/api/v1/appointments?")
    ? {body:{content:[appointment],totalPages:2,totalElements:11}} : listHandler(url,options),true);
  try{await until(()=>!ui.el("next-page").disabled);ui.el("next-page").click();
    await until(()=>ui.el("page-info").textContent.startsWith("หน้า 2") && ui.el("list-message").hidden);
    ui.change("status-filter","CANCELLED");
    await until(()=>ui.el("page-info").textContent.startsWith("หน้า 1") && ui.el("list-message").hidden);
    const filtered=ui.calls.findLast(c=>c.url.includes("status=CANCELLED"));assert.ok(filtered.url.includes("page=0"));
  }finally{ui.dom.window.close();}
});
