# UAS_PBO_SistemManufacture
<ul>
  <li>Mata Kuliah: Pemrograman Berorientasi Obyek 2</li>
  <li>Dosen Pengampu: <a href="https://github.com/Muhammad-Ikhwan-Fathulloh">Muhammad Ikhwan Fathulloh</a></li>
</ul>

## Kelompok 5
<ul>
  <li>Nama:  Aditia Nurwansyah</li>
  <p>NIM : 23552011329</p>
</ul>

<ul>
  <li>Nama: Luthfi Fathillah</li>
  <p>NIM : 23552011209</p>
</ul>

<ul>
  <li>Nama : Rajza Muhammad Yasyfa Fajri Sidiq</li>
  <p>NIM  : 23552011039</p>
</ul>

## Judul Studi Kasus
<p> Sistem Manufacture </p>

## Penjelasan Studi Kasus
<p>Manufactur sistem dan pengelolaan produk adalah aspek krusial dalam industri manufaktur.
Banyak perusahaan manufaktur skala kecil hingga menengah masih menggunakan sistem
manual atau terpisah-pisah, sehingga menyebabkan data tidak terintegrasi, proses menjadi
lambat, dan rawan kesalahan.
Proyek ini bertujuan untuk membangun aplikasi desktop sederhana menggunakan Java,
JavaFX, dan Maven, yang dapat membantu proses manufaktur, pengelolaan produk,
manajemen pesanan produksi, serta pengelolaan pengguna dalam satu sistem yang
terintegrasi.</p>

## Penjelasan 4 Pilar OOP dalam Studi Kasus

### 1. Inheritance
<p>AutomotiveProduct menggunakan extends Product, artinya kelas ini mewarisi sifat dan method
dari Product.java contoh nya: public class AutomotiveProduct extends Product {</p>

### 2. Encapsulation
<p>Kelas ini menggunakan private attributes seperti vehicleType, engineSpec, safetyRating dan
menyediakan getter & setter:java
public String getVehicleType() { return vehicleType; }
public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
Ini menunjukkan encapsulation, karena data dilindungi dengan akses terbatas melalui method.</p>

### 3. Polymorphism
<p>Method seperti calculateProductionCost(), updateQualityStatus(), dan
getProductionRequirements() ditandai dengan @Override, artinya mereka
mengimplementasikan perilaku berbeda dibandingkan versi default di kelas Product. Ini adalah
contoh runtime polymorphism.</p>

### 4. Abstract
<p>Karena ada @Override, kemungkinan besar Product adalah abstract class yang mendefinisikan
method abstract seperti calculateProductionCost() dan updateQualityStatus(). Kelas
AutomotiveProduct kemudian memberikan implementasi konkret.</p>  

## Demo Proyek
<ul>
  <li>Github: <a href="https://github.com/aditianurwansyah/UAS_PBO_SistemManufacture">Github</a></li>  
  <li>Youtube: <a href="">Youtube</a></li>  
</ul>
