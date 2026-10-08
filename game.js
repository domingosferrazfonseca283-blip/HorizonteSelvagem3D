import * as THREE from 'https://cdn.jsdelivr.net/npm/three@0.180.0/build/three.module.js';
import { PointerLockControls } from 'https://cdn.jsdelivr.net/npm/three@0.180.0/examples/jsm/controls/PointerLockControls.js';
import { VRButton } from 'https://cdn.jsdelivr.net/npm/three@0.180.0/examples/jsm/webxr/VRButton.js';

const SAVE_KEY='horizonte-selvagem-save-v2';
const scene=new THREE.Scene();
scene.background=new THREE.Color(0x9bb5b2);
scene.fog=new THREE.Fog(0x9bb5b2,35,170);
const camera=new THREE.PerspectiveCamera(70,innerWidth/innerHeight,.1,320);
camera.position.set(0,2,8);

const renderer=new THREE.WebGLRenderer({antialias:true});
renderer.setPixelRatio(Math.min(devicePixelRatio,2));
renderer.setSize(innerWidth,innerHeight);
renderer.shadowMap.enabled=true;
renderer.shadowMap.type=THREE.PCFSoftShadowMap;
renderer.xr.enabled=true;
document.querySelector('#game').appendChild(renderer.domElement);
const vrButton=VRButton.createButton(renderer);
vrButton.id='vrButton';
document.body.appendChild(vrButton);

scene.add(new THREE.HemisphereLight(0xdcecff,0x34433d,2.1));
const sun=new THREE.DirectionalLight(0xfff0cf,3);
sun.position.set(-30,50,20); sun.castShadow=true; sun.shadow.mapSize.set(2048,2048); scene.add(sun);

const controls=new PointerLockControls(camera,document.body);
const keys={};
let gameStarted=false;
let xp=0,level=1,energy=100,bond=0,target=null,combat=false,missionStep=0,velocityY=0,lastInteract=0;
const creatures=[];
const $=id=>document.getElementById(id);

function mat(c,rough=1){return new THREE.MeshStandardMaterial({color:c,roughness:rough})}

const ground=new THREE.Mesh(new THREE.PlaneGeometry(240,240),mat(0x46564a));
ground.rotation.x=-Math.PI/2; ground.receiveShadow=true; scene.add(ground);

function tree(x,z,s=1){
  const g=new THREE.Group();
  const trunk=new THREE.Mesh(new THREE.CylinderGeometry(.28*s,.4*s,3*s,8),mat(0x5b4330));
  trunk.position.y=1.5*s;
  const crown=new THREE.Mesh(new THREE.DodecahedronGeometry(2.1*s,1),mat(0x243f36));
  crown.position.y=4.1*s; crown.castShadow=true; g.add(trunk,crown);
  g.position.set(x,0,z); scene.add(g);
}
for(let i=0;i<85;i++){const a=Math.random()*Math.PI*2,r=15+Math.random()*88;tree(Math.cos(a)*r,Math.sin(a)*r,.55+Math.random()*1.3)}

function rock(x,z,s){
  const m=new THREE.Mesh(new THREE.IcosahedronGeometry(s,1),mat(0x59605b));
  m.position.set(x,s*.45,z);m.scale.y=.65;m.castShadow=true;scene.add(m);
}
for(let i=0;i<36;i++){const a=Math.random()*6.28,r=10+Math.random()*90;rock(Math.cos(a)*r,Math.sin(a)*r,.4+Math.random()*1.5)}

function createPath(){
  const path=new THREE.Mesh(new THREE.PlaneGeometry(7,150),mat(0x5b594b));
  path.rotation.x=-Math.PI/2; path.position.set(0,.012,-55); scene.add(path);
}
createPath();

function human(color=0x314c67){
  const g=new THREE.Group(),skin=mat(0xc58f70),cloth=mat(color);
  const body=new THREE.Mesh(new THREE.CapsuleGeometry(.38,.9,6,10),cloth);body.position.y=1.25;body.castShadow=true;g.add(body);
  const head=new THREE.Mesh(new THREE.SphereGeometry(.32,16,12),skin);head.position.y=2.15;head.castShadow=true;g.add(head);
  for(const x of [-.48,.48]){
    const arm=new THREE.Mesh(new THREE.CapsuleGeometry(.11,.7,5,8),cloth);arm.position.set(x,1.35,0);arm.rotation.z=x<0?.12:-.12;g.add(arm);
    const leg=new THREE.Mesh(new THREE.CapsuleGeometry(.14,.75,5,8),mat(0x202b31));leg.position.set(x*.55,.45,0);g.add(leg);
  }
  return g;
}
const playerEcho=human(0x3e6076);playerEcho.position.set(0,0,-3);playerEcho.rotation.y=Math.PI;scene.add(playerEcho);

function creature(x,z,name,kind){
  const g=new THREE.Group(),c=mat(kind===0?0x7d8068:kind===1?0x806d4e:0x666d6b),dark=mat(0x343c31);
  const body=new THREE.Mesh(new THREE.SphereGeometry(1,14,10),c);body.scale.set(1.25,.8,.9);body.position.y=1;g.add(body);
  const head=new THREE.Mesh(new THREE.SphereGeometry(.62,14,10),c);head.position.set(0,1.35,.95);g.add(head);
  for(const x2 of [-.55,.55])for(const z2 of [-.5,.5]){const l=new THREE.Mesh(new THREE.CapsuleGeometry(.1,.65,4,6),dark);l.position.set(x2,.55,z2);g.add(l)}
  g.position.set(x,0,z);
  g.userData={hp:100,maxHp:100,name,alive:true,phase:Math.random()*6,kind,bonded:false,trust:kind==='mistica'?20:0};
  scene.add(g);creatures.push(g);
}
creature(5,-10,'Mística','mistica');
creature(-13,-8,'Lince Solar',1);
creature(18,12,'Raposa de Cinza',2);

const particles=new THREE.Points(new THREE.BufferGeometry(),new THREE.PointsMaterial({color:0xdde8d2,size:.08,transparent:true,opacity:.6}));
const pa=[];for(let i=0;i<900;i++)pa.push((Math.random()-.5)*190,Math.random()*22,(Math.random()-.5)*190);
particles.geometry.setAttribute('position',new THREE.Float32BufferAttribute(pa,3));scene.add(particles);

function save(){
  localStorage.setItem(SAVE_KEY,JSON.stringify({xp,level,energy,bond,missionStep,misticaBonded:!!creatures[0]?.userData.bonded}));
}
function load(){
  try{
    const s=JSON.parse(localStorage.getItem(SAVE_KEY)||'null'); if(!s)return false;
    xp=Number.isFinite(s.xp)?s.xp:0;level=Number.isFinite(s.level)?s.level:1;energy=Number.isFinite(s.energy)?s.energy:100;
    bond=Number.isFinite(s.bond)?s.bond:0;missionStep=Number.isFinite(s.missionStep)?s.missionStep:0;
    if(s.misticaBonded&&creatures[0]){creatures[0].userData.bonded=true;creatures[0].userData.alive=true;}
    return true;
  }catch{return false}
}
function updateHud(){
  $('xp').textContent=xp;$('level').textContent=level;$('energy').textContent=Math.round(energy);$('bond').textContent=Math.round(bond);if($('trust'))$('trust').textContent=Math.round(creatures[0]?.userData.trust||0);
  const x=Math.round(camera.position.x),z=Math.round(-camera.position.z);$('coords').textContent=`${x}, ${z}`;
}
function setMission(text){$('mission').textContent=text}
function chapter(title,text,cb){
  $('chapterTitle').textContent=title;$('chapterText').textContent=text;$('chapter').classList.remove('hidden');
  $('chapterBtn').onclick=()=>{$('chapter').classList.add('hidden');if(cb)cb()};
}
function begin(){
  gameStarted=true;$('menu').classList.add('hidden');
  if(!load())chapter('O Primeiro Horizonte','Kael chega a uma região desconhecida seguindo um sinal de energia azul. O primeiro objetivo é descobrir de onde veio o chamado.',()=>setMission('Encontre o primeiro sinal de vida.'));
  else setMission(missionStep>=3?'Explore a região e encontre novos sinais de vida.':'Continue a investigação no vale.');
  updateHud();
}
$('startBtn').onclick=()=>{localStorage.removeItem(SAVE_KEY);xp=0;level=1;energy=100;bond=0;missionStep=0;begin()};
$('continueBtn').onclick=()=>begin();

function nearest(){
  let best=null,d=10;
  for(const c of creatures)if(c.userData.alive){const dd=c.position.distanceTo(camera.position);if(dd<d){d=dd;best=c}}
  return best;
}
function interact(){
  if(!gameStarted||performance.now()-lastInteract<500)return;lastInteract=performance.now();
  const c=nearest();
  if(c){
    target=c;
    if(c.userData.name==='Mística'&&!c.userData.bonded)
      $('dialogText').textContent='Mística observa Kael sem atacar. A confiança será construída com tempo, cuidado e experiências compartilhadas.';
    else $('dialogText').textContent=`${c.userData.name} observa você. A energia do ambiente parece alterar o comportamento da criatura.`;
    $('dialog').classList.remove('hidden');
    $('dialogBtn').onclick=()=>{$('dialog').classList.add('hidden');c.userData.bonded=true;c.userData.trust=Math.min(100,(c.userData.trust||0)+15);bond=Math.min(100,bond+10);gainXp(25);setMission(c.userData.name+' está começando a confiar em você. Continue explorando juntos.');updateHud();save()};
  }else setMission('Nenhum sinal próximo. Siga pelo caminho e explore o vale.');
}
function startCombat(){
  const c=nearest();if(!c)return;target=c;combat=true;
  $('combat').classList.remove('hidden');$('enemyName').textContent=c.userData.name;updateCombat();
}
function updateCombat(){$('enemyHp').style.width=Math.max(0,target?.userData.hp||0)+'%'}
function gainXp(amount){
  xp+=amount;
  while(xp>=100){xp-=100;level++;energy=100;setMission(`Nível ${level}! Sua energia foi restaurada.`)}
}
function action(a){
  if(!combat||!target)return;
  if(a==='guard'){energy=Math.min(100,energy+10);if(target?.userData)target.userData.trust=Math.min(100,(target.userData.trust||0)+5);setMission('Você cuidou da criatura e recuperou energia.')}
  else if(a==='pulse'){
    const dmg=18+Math.random()*16;target.userData.hp=Math.max(0,target.userData.hp-dmg);energy=Math.max(0,energy-8);
    setMission('Pulso emitido. A criatura está ficando mais calma.');
  }else if(target.userData.hp<55){
    target.userData.bonded=true;bond=Math.min(100,bond+25);gainXp(80);energy=Math.max(0,energy-15);combat=false;
    $('combat').classList.add('hidden');
    if(target.userData.name==='Mística'){missionStep=Math.max(missionStep,2);setMission('Mística aceitou o vínculo. Explore o caminho azul.');chapter('Mística','O vínculo é formado. Uma visão aponta para uma região além das montanhas. O próximo objetivo é investigar o caminho azul.',()=>save())}
    else setMission(`Vínculo estabelecido com ${target.userData.name}.`);
    save();
  }else setMission('A criatura ainda está instável. Use PULSO antes de tentar o VÍNCULO.');
  updateCombat();updateHud();save();
}
document.querySelectorAll('[data-action]').forEach(b=>b.onclick=()=>action(b.dataset.action));

addEventListener('keydown',e=>{
  keys[e.code]=true;
  if(e.code==='KeyE')interact();
  if(e.code==='KeyF')startCombat();
  if(e.code==='Escape'&&gameStarted){$('menu').classList.toggle('hidden')}
});
addEventListener('keyup',e=>keys[e.code]=false);
document.body.addEventListener('click',()=>{if(gameStarted&&!renderer.xr.isPresenting)controls.lock()});

const clock=new THREE.Clock();
function animate(){
  const dt=Math.min(clock.getDelta(),.05);
  if(gameStarted&&!renderer.xr.isPresenting){
    const speed=(keys.ShiftLeft||keys.ShiftRight?8:4)*dt;
    const dir=new THREE.Vector3((keys.KeyD?1:0)-(keys.KeyA?1:0),0,(keys.KeyS?1:0)-(keys.KeyW?1:0));
    if(dir.lengthSq())dir.normalize();
    controls.moveRight(dir.x*speed);controls.moveForward(-dir.z*speed);
    if(keys.Space&&camera.position.y<=2.01)velocityY=7.2;
    velocityY-=18*dt;camera.position.y+=velocityY*dt;
    if(camera.position.y<2){camera.position.y=2;velocityY=0}
  }
  if(gameStarted){
    const day=Math.sin(clock.elapsedTime*.035)*.5+.5;
    sun.intensity=1.1+day*2.1;
    scene.background.setHSL(.48,.08,.32+day*.14);
    scene.fog.color.copy(scene.background);
    for(const c of creatures)if(c.userData.alive){
      c.rotation.y+=Math.sin(clock.elapsedTime+c.userData.phase)*dt*.15;
      c.position.x+=Math.sin(clock.elapsedTime*.5+c.userData.phase)*dt*.12;
      c.position.z+=Math.cos(clock.elapsedTime*.4+c.userData.phase)*dt*.12;
    }
    particles.rotation.y+=dt*.01;updateHud();
  }
  renderer.render(scene,camera);
}
renderer.setAnimationLoop(animate);
addEventListener('resize',()=>{camera.aspect=innerWidth/innerHeight;camera.updateProjectionMatrix();renderer.setSize(innerWidth,innerHeight)});
updateHud();
setTimeout(()=>{const l=$('loading');if(l)l.remove()},700);
