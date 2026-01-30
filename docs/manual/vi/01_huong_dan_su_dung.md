# He thong Can thong minh Busan - Huong dan Su dung

| Muc | Noi dung |
|------|------|
| **Ten tai lieu** | Huong dan Su dung He thong Can thong minh Busan |
| **Phien ban** | 1.3 |
| **Ngay cap nhat cuoi** | 2026-01-30 |
| **Doi tuong doc gia** | Tai xe (DRIVER), Nhan vien phu trach (MANAGER) |
| **Muc do bao mat** | Noi bo |

---

## Muc luc

1. [Gioi thieu he thong](#1-gioi-thieu-he-thong)
2. [Bat dau su dung](#2-bat-dau-su-dung)
3. [Chuc nang tai xe](#3-chuc-nang-tai-xe)
4. [Chuc nang nhan vien phu trach](#4-chuc-nang-nhan-vien-phu-trach)
5. [Chuc nang chung](#5-chuc-nang-chung)
   - 5.1 Trang ca nhan (Xem/sua ho so, doi mat khau, cai dat thong bao)
   - 5.2 Thong bao
   - 5.3 Thong bao chung (Loc danh muc, tim kiem, ghim)
   - 5.4 Tro giup/FAQ (Cau hoi thuong gap theo danh muc)
   - 5.5 Lien he/Khieu nai
   - 5.6 Yeu thich (Web)
   - 5.7 Chuyen doi giao dien Toi/Sang
   - 5.8 Phim tat va quan ly tab (Web)
6. [Cau hoi thuong gap (FAQ)](#6-cau-hoi-thuong-gap-faq)
7. [Huong dan xu ly su co](#7-huong-dan-xu-ly-su-co)
8. [Thuat ngu](#8-thuat-ngu)
9. [Lich su thay doi](#lich-su-tai-lieu)

---

## 1. Gioi thieu he thong

### 1.1 Tong quan

**He thong Can thong minh Busan** la giai phap tich hop tu dong hoa nghiep vu can xe tai nha may thep Busan. He thong ket hop cong nghe LPR (Nhan dien bien so xe) va AI de tu dong nhan dien bien so khi xe vao tram can, doi chieu thong tin dieu phoi va thuc hien can.

He thong nay huong den viec so hoa nghiep vu can truoc day thuc hien bang tay, nang cao hieu qua cong viec va dam bao do chinh xac, minh bach cua du lieu.

### 1.2 Dac diem chinh

| Dac diem | Mo ta |
|------|------|
| **Can tu dong LPR** | Nhan dien tu dong bien so xe bang AI (do chinh xac tren 95%), thuc hien can tu dong |
| **OTP di dong du phong** | Ho tro can thay the qua OTP tren dien thoai khi LPR nhan dien that bai |
| **Phieu can dien tu** | Tu dong tao va chia se phieu can dien tu thay the phieu can giay |
| **Giam sat thoi gian thuc** | Giam sat trang thai thiet bi va tinh hinh can thoi gian thuc qua WebSocket |
| **Thong ke/Phan tich** | Thong ke can theo ngay, thang, loai hang, doanh nghiep va tai xuong Excel |

### 1.3 Cau hinh he thong

He thong cung cap hai phuong thuc truy cap:

- **He thong quan ly web**: Giao dien quan ly truy cap qua trinh duyet PC (chu yeu cho nhan vien phu trach)
- **Ung dung di dong**: Ung dung cai dat tren dien thoai thong minh (chu yeu cho tai xe)

### 1.4 Vai tro nguoi dung

He thong ho tro ba vai tro nguoi dung. Tai lieu nay huong dan chu yeu cho vai tro **Tai xe** va **Nhan vien phu trach**.

| Vai tro | Mo ta | Moi truong su dung chinh |
|------|------|----------------|
| **Quan tri vien (ADMIN)** | Cau hinh toan bo he thong va quan ly nguoi dung | Web |
| **Nhan vien phu trach (MANAGER)** | Quan ly dieu phoi, can, xuat cong, du lieu goc, thong ke | Web |
| **Tai xe (DRIVER)** | Xem dieu phoi, thuc hien can, xem phieu can dien tu | Ung dung di dong |

### 1.5 Quy trinh nghiep vu can

Toan bo nghiep vu can duoc thuc hien theo cac buoc sau:

```
Dang ky dieu phoi -> Xe vao -> Can lan 1 (xe khong) -> Xep/do hang -> Can lan 2 (xe co hang) -> Phat hanh phieu can dien tu -> Xuat cong
```

**Luu y**: Tuy theo loai hang, co the can them lan 3. Trong luong tinh duoc tu dong tinh theo cong thuc |Trong luong lan 1 - Trong luong lan 2|.

---

## 2. Bat dau su dung

### 2.1 Yeu cau he thong

#### He thong quan ly web (Danh cho nhan vien phu trach)

| Muc | Yeu cau |
|------|----------|
| **He dieu hanh** | Windows 10 tro len, macOS 12 tro len |
| **Trinh duyet** | Chrome 90 tro len (khuyen nghi), Edge 90 tro len, Firefox 90 tro len |
| **Do phan giai man hinh** | Khuyen nghi 1920x1080 tro len |
| **Mang** | Bat buoc ket noi mang noi bo |
| **Khac** | Bat JavaScript, tat chan pop-up |

#### Ung dung di dong (Danh cho tai xe)

| Muc | Yeu cau |
|------|----------|
| **Android** | Android 10 (API 29) tro len |
| **iOS** | iOS 14.0 tro len |
| **Dung luong luu tru** | Toi thieu 100MB dung luong trong |
| **Mang** | Ket noi du lieu di dong hoac Wi-Fi |
| **Quyen** | Can cap quyen camera va thong bao |

### 2.2 Dang nhap

#### 2.2.1 Dang nhap web (Nhan vien phu trach)

1. Mo trinh duyet va truy cap dia chi he thong.
2. **Man hinh dang nhap** (`/login`) se duoc hien thi.
3. Nhap ID nguoi dung da duoc cap vao o **Ten dang nhap**.
4. Nhap mat khau vao o **Mat khau**.
5. Nhan nut **Dang nhap**.
6. Khi xac thuc thanh cong, he thong se chuyen den man hinh **Bang dieu khien** (`/dashboard`).

> **Luu y**
> - Nhan sai mat khau 5 lan lien tiep, tai khoan se bi **khoa trong 30 phut**.
> - Khi tai khoan bi khoa, se tu dong mo sau 30 phut. Neu can mo khoa ngay, vui long lien he quan tri vien.
> - Sau khi dang nhap, neu khong thao tac trong **30 phut** se tu dong dang xuat (access token het han). Khi truy cap lai trong vong 7 ngay, token se tu dong lam moi.

#### 2.2.2 Dang nhap ung dung di dong (Tai xe)

1. Mo ung dung **Can thong minh Busan** tren dien thoai.
2. Nhap **Ten dang nhap** va **Mat khau**.
3. Nhan nut **Dang nhap**.
4. Khi xac thuc thanh cong, he thong se chuyen den man hinh chinh.

> **Luu y**: Ung dung di dong ho tro tinh nang tu dong dang nhap. Sau lan dang nhap dau tien, tu dong dang nhap se duy tri trong 7 ngay (dua tren refresh token).

#### 2.2.3 Dang nhap OTP tren di dong

Khi LPR khong nhan dien tu dong duoc, ban co the thuc hien can bang OTP (mat khau su dung mot lan) hien thi tren bang dien tu LED tai hien truong.

1. Sau khi vao tram can, kiem tra **ma OTP 6 chu so** hien thi tren bang dien tu LED.
2. Chon menu **Can OTP** tren ung dung di dong.
3. Nhap ma OTP 6 chu so.
4. Nhan nut **Xac nhan**.

> **Luu y**
> - OTP chi co hieu luc trong **5 phut** sau khi cap. Khi het thoi gian, OTP moi se duoc cap.
> - Nhap sai OTP **3 lan lien tiep**, OTP do se bi vo hieu hoa. Vui long lien he nhan vien phu trach can.

### 2.3 Dang xuat

#### Dang xuat web

1. Nhan vao **bieu tuong nguoi dung** goc tren ben phai man hinh.
2. Chon **Dang xuat** tu menu xo xuong.
3. He thong se chuyen ve man hinh dang nhap (`/login`).

#### Dang xuat ung dung di dong

1. Chon tab **Trang ca nhan** o thanh dieu huong duoi cung.
2. Nhan nut **Dang xuat**.
3. Nhan **Xac nhan** tren cua so xac nhan.

### 2.4 Huong dan bo cuc man hinh

#### 2.4.1 Bo cuc man hinh web (Nhan vien phu trach)

Bo cuc man hinh he thong quan ly web nhu sau:

```
+------------------------------------------------------------------+
|  [Logo]     He thong Can thong minh Busan   [Thong bao] [Menu]   |  <- Thanh tieu de
+----------+-------------------------------------------------------+
|          |                                                        |
| Bang dieu|                                                        |
|  khien   |              Vung noi dung chinh                       |
| Dieu phoi|                                                        |
| Quan ly  |                                                        |
|  can     |                                                        |
| Phieu can|                                                        |
| Xuat cong|                                                        |
| Du lieu  |                                                        |
|  goc   > |                                                        |
| Thong ke |                                                        |
| Giam sat |                                                        |
| Thong bao|                                                        |
| Tro giup |                                                        |
|          |                                                        |
+----------+-------------------------------------------------------+
```

| Vung | Mo ta |
|------|------|
| **Thanh tieu de** | Logo he thong, bieu tuong ngoi sao yeu thich (dang ky/huy yeu thich trang hien tai), nut chuyen doi giao dien (chuyen doi Toi/Sang), bieu tuong thong bao (hien thi so thong bao chua doc), menu nguoi dung (trang ca nhan, dang xuat) |
| **Thanh dieu huong ben trai** | Danh sach menu chinh. Du lieu goc bao gom menu con (doanh nghiep, xe, can, ma chung) |
| **Vung noi dung** | Vung hien thi chuc nang chi tiet cua menu duoc chon |

> **Luu y**: He thong web ho tro **che do toi** va **che do sang**. Nhan vao **nut chuyen doi giao dien** tren thanh tieu de de chuyen doi giua che do toi va che do sang. Cai dat giao dien duoc tu dong luu va duy tri khi truy cap lan sau. Xem chi tiet tai [5.7 Chuyen doi giao dien Toi/Sang](#57-chuyen-doi-giao-dien-toisang).
>
> **Luu y**: Chuong trinh CS (chuong trinh hien truong tram can) cung ho tro giao dien toi/sang. Nhan vao bieu tuong chuyen doi giao dien tren thanh tieu de de chuyen doi. Cai dat duoc tu dong luu.

#### 2.4.2 Bo cuc man hinh ung dung di dong (Tai xe)

```
+---------------------------+
|  Can thong minh Busan      |  <- Thanh tren
+---------------------------+
|                           |
|    Vung noi dung chinh     |
|                           |
|                           |
+---------------------------+
| Home | Dieu phoi | Can | Ca nhan |  <- Thanh dieu huong duoi
+---------------------------+
```

| Vung | Mo ta |
|------|------|
| **Thanh tren** | Tieu de man hinh, nut quay lai, bieu tuong thong bao |
| **Vung noi dung** | Hien thi noi dung chi tiet cua tung chuc nang |
| **Thanh dieu huong duoi** | Cac tab: Trang chu, Xem dieu phoi, Tinh hinh can, Trang ca nhan |

#### 2.4.3 Huong dan cau truc trang

| Trang | URL | Chuc nang | Doi tuong |
|--------|-----|------|------|
| Dang nhap | `/login` | Xac thuc nguoi dung | Chung |
| Bang dieu khien | `/dashboard` | Tong quan va du lieu thoi gian thuc | Nhan vien phu trach |
| Quan ly dieu phoi | `/dispatch` | Dang ky/Xem/Sua/Xoa dieu phoi | Nhan vien phu trach |
| Quan ly can | `/weighing` | Xem va quan ly ban ghi can | Nhan vien phu trach |
| Trang thai tram can | `/weighing-station` | Giam sat tram can thoi gian thuc | Nhan vien phu trach |
| Phieu can dien tu | `/slip` | Xem va chia se phieu can dien tu | Chung |
| Quan ly xuat cong | `/gate-pass` | Duyet/Tu choi phep xuat cong | Nhan vien phu trach |
| Thong ke | `/statistics` | Bao cao va phan tich | Nhan vien phu trach |
| Giam sat | `/monitoring` | Giam sat trang thai thiet bi | Nhan vien phu trach |
| Thong bao | `/notice` | Xem thong bao | Chung |
| Lien he | `/inquiry` | Dang ky va xem lien he/khieu nai | Chung |
| Trang ca nhan | `/mypage` | Thong tin ca nhan va cai dat | Chung |
| Du lieu goc - Doanh nghiep | `/master/company` | Quan ly doi tac | Nhan vien phu trach |
| Du lieu goc - Xe | `/master/vehicle` | Quan ly thong tin xe | Nhan vien phu trach |
| Du lieu goc - Can | `/master/scale` | Quan ly can | Nhan vien phu trach |
| Du lieu goc - Ma chung | `/master/code` | Quan ly ma chung | Nhan vien phu trach |
| Tro giup/FAQ | `/help` | Cau hoi thuong gap | Chung |

---

## 3. Chuc nang tai xe

Tai xe chu yeu su dung he thong qua **ung dung di dong**. Cac chuc nang chinh bao gom xem dieu phoi, thuc hien can, xem va chia se phieu can dien tu.

### 3.1 Xem dieu phoi

Chuc nang xem dieu phoi cho phep tai xe xem cac chuyen van tai duoc phan cong.

#### Cach xem dieu phoi

1. Chon tab **Dieu phoi** o thanh dieu huong duoi cua ung dung di dong.
2. Danh sach dieu phoi se duoc hien thi. Moi muc bao gom cac thong tin sau:

   | Muc | Mo ta |
   |------|------|
   | So dieu phoi | Ma dinh danh dieu phoi duy nhat |
   | Trang thai | Da dang ky (REGISTERED), Dang thuc hien (IN_PROGRESS), Hoan thanh (COMPLETED), Da huy (CANCELLED) |
   | Loai hang | San pham phu, chat thai, vat lieu phu, xuat kho, chung |
   | Noi xep/do hang | Thong tin noi di va noi den |
   | Ngay dieu phoi | Ngay du kien dieu phoi |
   | Bien so xe | Bien so xe duoc phan cong |

3. Nhan vao mot dieu phoi cu the de chuyen den **man hinh chi tiet dieu phoi** va xem thong tin chi tiet.

#### Giai thich trang thai dieu phoi

```
Da dang ky (REGISTERED) -> Dang thuc hien (IN_PROGRESS) -> Hoan thanh (COMPLETED)
                                                         -> Da huy (CANCELLED)
```

| Trang thai | Mo ta |
|------|------|
| **Da dang ky (REGISTERED)** | Trang thai ban dau khi nhan vien phu trach dang ky dieu phoi. Chua bat dau can |
| **Dang thuc hien (IN_PROGRESS)** | Quy trinh can dang dien ra, bat dau tu can lan 1 |
| **Hoan thanh (COMPLETED)** | Tat ca cac lan can da hoan thanh va phieu can dien tu da duoc phat hanh |
| **Da huy (CANCELLED)** | Dieu phoi da bi huy. Ly do co the xem tai man hinh chi tiet |

> **Luu y**: Danh sach dieu phoi mac dinh sap xep theo ngay dieu phoi gan nhat. Su dung bo loc o phia tren de xem theo trang thai.

### 3.2 Can tu dong LPR

Can tu dong LPR (License Plate Recognition) la chuc nang cot loi, trong do AI tu dong nhan dien bien so khi xe vao tram can de thuc hien can. Tai xe khong can thao tac gi, quy trinh dien ra tu dong.

#### Quy trinh can tu dong

Sau day la quy trinh tu dong dien ra sau khi xe vao tram can:

**Buoc 1: Phat hien xe vao**
- Khi xe vao tram can, **cam bien LiDAR** tu dong phat hien su hien dien cua xe.
- Bang dien tu LED hien thi thong bao "Dang phat hien xe...".

**Buoc 2: Nhan dien bien so**
- Camera LPR chup anh bien so xe.
- AI phan tich hinh anh bien so de nhan dien so xe (do chinh xac tren 95%).

**Buoc 3: Tu dong doi chieu dieu phoi**
- He thong tu dong tim kiem va doi chieu thong tin dieu phoi da dang ky dua tren so xe nhan dien duoc.
- Khi doi chieu thanh cong, bang dien tu LED hien thi bien so xe va thong tin dieu phoi.

**Buoc 4: Do trong luong**
- Bo chi thi can (indicator) doc du lieu trong luong qua giao tiep RS-232C.
- Khi gia tri trong luong on dinh, gia tri do se tu dong duoc xac nhan.

**Buoc 5: Luu du lieu va phat hanh phieu can**
- Trong luong do duoc se tu dong luu len may chu.
- Neu la lan can cuoi cung (lan 2 hoac lan 3), **phieu can dien tu** se tu dong duoc tao.
- Thong bao se duoc gui den ung dung di dong.

**Buoc 6: Xe ra**
- Bang dien tu LED hien thi thong bao "Hoan thanh can" va trong luong do duoc.
- Khi thanh chan mo, hay di chuyen xe ra an toan.

#### Nhung dieu tai xe can kiem tra

De quy trinh can tu dong dien ra thuan loi, vui long kiem tra cac muc sau:

1. Dam bao **bien so xe sach** (do ban, che khuat se lam giam ty le nhan dien).
2. **Dung xe chinh xac** tren tram can.
3. Trong khi can, **khong tat may** va cho doi.
4. Kiem tra thong bao huong dan tren bang dien tu LED va lam theo chi dan.
5. Chi di chuyen xe ra sau khi hien thi thong bao "Hoan thanh can".

> **Canh bao**: Neu xuong xe hoac di chuyen xe trong khi can, he thong khong the do trong luong chinh xac. Nhat dinh phai kiem tra thong bao hoan thanh can truoc khi di chuyen xe ra.

#### Huong dan thu tu can

| Lan can | Mo ta | Trang thai thong thuong |
|-----------|------|---------------|
| **Can lan 1** | Do trong luong lan dau (thuong la trong luong xe khong) | Truoc khi xep hang hoac truoc khi do hang |
| **Can lan 2** | Do trong luong lan hai (thuong la trong luong xe co hang) | Sau khi xep hang hoac sau khi do hang |
| **Can lan 3** | Can bo sung khi can thiet | Hang dac biet hoac can lai |

> **Luu y**: **Trong luong tinh (Net Weight)** = |Trong luong lan 1 - Trong luong lan 2| duoc tu dong tinh.

### 3.3 Can bang OTP di dong

Khi LPR khong nhan dien tu dong duoc hoac trong tinh huong dac biet, ban co the su dung OTP tren di dong de thuc hien can.

#### Cac truong hop can su dung OTP

- Khi LPR khong nhan dien duoc do bien so ban, hong, bi che
- Xe su dung bien so tam
- Khi he thong tam thoi loi va khong the nhan dien tu dong

#### Quy trinh can OTP

**Buoc 1: Xac nhan ma OTP**
1. Sau khi xe vao tram can, neu LPR khong nhan dien duoc, bang dien tu LED se hien thi **ma OTP 6 chu so**.
2. Ghi nho hoac ghi lai ma OTP duoc hien thi.

**Buoc 2: Nhap OTP tren ung dung di dong**
1. Mo ung dung di dong.
2. Nhan nut **Can OTP** o man hinh chinh hoac tab Can.
3. Nhap ma OTP 6 chu so.
4. Nhan nut **Xac nhan**.

**Buoc 3: Xac minh danh tinh**
1. He thong tien hanh xac minh danh tinh qua so dien thoai da dang ky.
2. Khi xac minh hoan thanh, he thong tu dong chuyen sang buoc tiep theo.

**Buoc 4: Chon dieu phoi**
1. Danh sach dieu phoi da phan cong cho ban se duoc hien thi.
2. Chon **chuyen dieu phoi** can can hien tai.
3. Nhan nut **Bat dau can**.

**Buoc 5: Thuc hien can**
1. He thong lien ket voi bo chi thi can de do trong luong.
2. Trong luong do duoc se hien thi tren man hinh ung dung.
3. Xac nhan gia tri trong luong va nhan nut **Xac nhan**.

**Buoc 6: Hoan thanh can**
1. Du lieu can duoc luu len may chu.
2. Neu la lan can cuoi cung, phieu can dien tu se tu dong duoc tao.
3. Bang dien tu LED hien thi thong bao "Hoan thanh can".
4. Di chuyen xe ra an toan.

> **Luu y**
> - Ma OTP chi co hieu luc trong **5 phut** sau khi cap. Khi het thoi gian, vui long kiem tra OTP moi tren bang dien tu LED.
> - Nhap sai OTP **3 lan lien tiep**, OTP do se bi vo hieu hoa. Trong truong hop nay, vui long lien he nhan vien phu trach can (phong dieu do) de yeu cau xu ly thu cong.
> - Neu ung dung bi dong trong khi can OTP, ban phai thu lai tu dau.

### 3.4 Xem va chia se phieu can dien tu

Khi can hoan thanh, phieu can dien tu se tu dong duoc tao. Tai xe co the xem va chia se phieu can tren ung dung di dong.

#### 3.4.1 Xem phieu can dien tu

1. Chon tab **Can** o thanh dieu huong duoi cua ung dung di dong.
2. Danh sach can da hoan thanh se duoc hien thi.
3. Nhan vao chuyen can muon xem.
4. Nhan nut **Xem phieu can dien tu**.
5. Phieu can dien tu se hien thi tren man hinh.

Phieu can dien tu bao gom cac thong tin sau:

| Muc | Mo ta |
|------|------|
| So phieu can | Ma dinh danh duy nhat |
| So dieu phoi | So dieu phoi lien quan |
| Bien so xe | Bien so xe can |
| Thong tin tai xe | Ten tai xe, doanh nghiep truc thuoc |
| Loai hang | San pham phu, chat thai, vat lieu phu, xuat kho, chung |
| Trong luong lan 1 | Gia tri can lan dau (kg) |
| Trong luong lan 2 | Gia tri can lan hai (kg) |
| Trong luong lan 3 | Gia tri can lan ba (neu co, kg) |
| Trong luong tinh | Trong luong tinh tu dong tinh (kg) |
| Thoi gian can | Thoi gian can tung lan |
| Che do can | LPR (tu dong), Di dong (OTP), Thu cong (manual) |
| Ghi chu | Cac luu y dac biet |

#### 3.4.2 Chia se phieu can dien tu

Ban co the chia se phieu can dien tu cho doi tac hoac nguoi lien quan.

**Chia se qua KakaoTalk**

1. Nhan nut **Chia se** tren man hinh chi tiet phieu can dien tu.
2. Chon **KakaoTalk** tren man hinh chon phuong thuc chia se.
3. Ung dung KakaoTalk se mo, chon doi tuong chia se (ban be hoac phong chat).
4. Nhan nut **Gui**.

**Chia se qua SMS**

1. Nhan nut **Chia se** tren man hinh chi tiet phieu can dien tu.
2. Chon **SMS** tren man hinh chon phuong thuc chia se.
3. Nhap so dien thoai nguoi nhan hoac chon tu danh ba.
4. Nhan nut **Gui**.

> **Luu y**: Khi chia se, noi dung bao gom cac thong tin chinh cua phieu can dien tu (so phieu can, bien so xe, trong luong tinh, thoi gian can) va lien ket de xem chi tiet.

### 3.5 Yeu thich

Ban co the them dieu phoi hoac doanh nghiep thuong xem vao danh sach yeu thich de truy cap nhanh.

#### Them vao yeu thich

1. Di chuyen den man hinh chi tiet cua muc muon them (danh sach dieu phoi, thong tin doanh nghiep...).
2. Nhan vao **bieu tuong ngoi sao** (☆) tren man hinh.
3. Khi bieu tuong chuyen thanh ngoi sao to (★), muc do da duoc them vao yeu thich.
4. Nhan lai de huy yeu thich.

#### Xem danh sach yeu thich

1. Kiem tra khu vuc **Yeu thich** tren man hinh chinh (Home) cua ung dung di dong.
2. Cac muc yeu thich da dang ky duoc hien thi theo loai (dieu phoi, doanh nghiep, xe).
3. Nhan vao muc de chuyen truc tiep den man hinh chi tiet.

> **Luu y**: Ban co the thay doi thu tu danh sach yeu thich. Nhan giu muc trong danh sach de keo va sap xep lai thu tu.
>
> **Luu y**: Chuc nang yeu thich tren he thong web duoc huong dan tai [5.6 Yeu thich (Web)](#56-yeu-thich-web).

### 3.6 Xem thong bao

Ban co the nhan thong tin quan trong ve can theo thoi gian thuc qua thong bao tu he thong.

1. Nhan vao **bieu tuong thong bao** (hinh chuong) o phia tren ung dung di dong.
2. Danh sach thong bao se duoc hien thi.
3. Thong bao chua doc se duoc lam noi bat.
4. Nhan vao thong bao de chuyen den man hinh tuong ung (chi tiet dieu phoi, phieu can...).

Cac loai thong bao chinh:

| Loai thong bao | Noi dung |
|-----------|------|
| Thong bao dieu phoi | Khi co dieu phoi moi duoc phan cong |
| Hoan thanh can | Khi can tu dong hoan thanh |
| Phat hanh phieu can | Khi phieu can dien tu duoc tao |
| Thong bao chung | Khi co thong bao moi duoc dang |
| Thong bao he thong | Huong dan lien quan tai khoan, bao tri he thong... |

### 3.7 Lien he (Goi dien)

Khi gap van de trong qua trinh su dung he thong hoac can ho tro, ban co the su dung chuc nang lien he de goi dien truc tiep den bo phan phu trach.

1. Nhan **Lien he** tren man hinh chinh hoac trang ca nhan cua ung dung di dong.
2. Chon doi tuong lien he.

   | Doi tuong lien he | Nghiep vu phu trach |
   |-----------|-----------|
   | **Phong dieu do** | Hoi ve can, loi OTP, su co thiet bi... |
   | **Kho vat tu** | Hoi ve hang hoa, xep/do hang... |

3. Nhan nut **Goi dien** cua bo phan da chon, ung dung dien thoai se duoc khoi dong.
4. Khi cuoc goi ket thuc, he thong quay lai ung dung va **lich su cuoc goi** se tu dong duoc luu.
5. Lich su cuoc goi truoc do co the xem tai danh sach **Lich su cuoc goi** tren man hinh lien he.

> **Luu y**: Trong tinh huong khan cap (hong thiet bi, van de an toan...), hay goi ngay cho phong dieu do.

### 3.8 Xem lich su can

Ban co the xem lich su can va dieu phoi truoc day tren ung dung di dong.

1. Chon tab **Trang chu** o thanh dieu huong duoi cua ung dung di dong.
2. Nhan menu **Xem lich su**.
3. So luong can hoan thanh theo thoi ky va lich su dieu phoi se duoc hien thi.
4. Nhan vao muc cu the de xem thong tin chi tiet cua chuyen can hoac dieu phoi do.

---

## 4. Chuc nang nhan vien phu trach

Nhan vien phu trach quan ly toan bo nghiep vu can bao gom dieu phoi, can, xuat cong, du lieu goc, thong ke, giam sat thong qua **he thong quan ly web**.

### 4.1 Bang dieu khien

Bang dieu khien (`/dashboard`) la man hinh dau tien hien thi sau khi dang nhap, cho phep nam bat tong quan tinh hinh nghiep vu can.

#### Cac thanh phan bang dieu khien

| Thanh phan | Mo ta |
|-----------|------|
| **Tinh hinh can hom nay** | Hien thi tong so chuyen can, so hoan thanh, so dang thuc hien trong ngay |
| **Tinh hinh can thang** | Tong so chuyen can va tong trong luong luy ke trong thang |
| **Bieu do thoi gian thuc** | Hien thi xu huong can theo khung gio bang do thi thoi gian thuc (dua tren ECharts) |
| **Thong ke theo doanh nghiep** | Tom tat ket qua can theo doanh nghiep (so chuyen, trong luong) |
| **Can dang thuc hien** | Danh sach can dang thuc hien tai tram can (cap nhat thoi gian thuc) |
| **Thong bao** | Tom tat thong bao gan day (toi da 5 muc, thong bao ghim hien thi truoc) |
| **Tom tat trang thai thiet bi** | Trang thai cac thiet bi chinh: can, camera LPR, LiDAR... |

#### Cach su dung bang dieu khien

1. Nhan **Bang dieu khien** o menu ben trai hoac man hinh se tu dong hien thi sau khi dang nhap.
2. Nhan vao tung the hien trang de chuyen den man hinh chi tiet tuong ung.
   - Nhan "Tinh hinh can hom nay" de chuyen den man hinh quan ly can (`/weighing`)
   - Nhan vao muc "Can dang thuc hien" de hien thi thong tin chi tiet chuyen can do
   - Nhan "Thong bao" de chuyen den man hinh thong bao (`/notice`)
3. Bieu do thoi gian thuc tu dong cap nhat qua **WebSocket**, khong can lam moi trang.

> **Luu y**: Du lieu bang dieu khien duoc cap nhat theo thoi gian thuc. Neu man hinh co van de, hay lam moi trinh duyet (F5).

### 4.2 Quan ly dieu phoi

Quan ly dieu phoi (`/dispatch`) la chuc nang dang ky va quan ly cac chuyen van tai.

#### 4.2.1 Dang ky dieu phoi

1. Nhan **Quan ly dieu phoi** o menu ben trai.
2. Nhan nut **Dang ky moi** o goc tren ben phai man hinh danh sach dieu phoi.
3. Nhap cac thong tin sau vao bieu mau dang ky dieu phoi:

   | Muc nhap | Bat buoc | Mo ta |
   |-----------|-----------|------|
   | Ngay dieu phoi | Bat buoc | Ngay du kien dieu phoi (chon tu lich) |
   | Loai hang | Bat buoc | Chon: san pham phu, chat thai, vat lieu phu, xuat kho, chung |
   | Doanh nghiep | Bat buoc | Chon doanh nghiep da dang ky trong du lieu goc |
   | Bien so xe | Bat buoc | Chon xe da dang ky trong du lieu goc |
   | Tai xe | Bat buoc | Chon tai xe duoc phan cong cho xe |
   | Noi xep hang | Bat buoc | Noi xuat phat (noi xep hang) |
   | Noi do hang | Bat buoc | Noi den (noi do hang) |
   | Ghi chu | Tuy chon | Ghi cac luu y dac biet |

4. Kiem tra noi dung nhap, sau do nhan nut **Luu**.
5. Thong bao xac nhan "Dieu phoi da duoc dang ky." se hien thi.
6. Dieu phoi da dang ky se duoc them vao danh sach voi trang thai **Da dang ky (REGISTERED)**.

> **Canh bao**: Neu cung mot xe co dieu phoi dang thuc hien trong cung ngay, thong bao canh bao trung lap co the hien thi. Vui long kiem tra truoc khi tien hanh dang ky.

#### 4.2.2 Xem va tim kiem dieu phoi

1. Nhan **Quan ly dieu phoi** o menu ben trai.
2. Danh sach dieu phoi duoc hien thi dang bang.
3. Su dung cac dieu kien tim kiem o phia tren de tim dieu phoi mong muon.

   | Dieu kien tim kiem | Mo ta |
   |-----------|------|
   | Thoi gian | Ngay bat dau ~ ngay ket thuc theo ngay dieu phoi |
   | Trang thai | Da dang ky, dang thuc hien, hoan thanh, da huy (co the chon nhieu) |
   | Loai hang | San pham phu, chat thai, vat lieu phu, xuat kho, chung |
   | Doanh nghiep | Tim theo ten doanh nghiep |
   | Bien so xe | Tim theo bien so xe |
   | Tai xe | Tim theo ten tai xe |

4. Sau khi nhap dieu kien, nhan nut **Tim kiem**.
5. Nhan nut **Dat lai** de xoa tat ca dieu kien tim kiem.

#### 4.2.3 Sua dieu phoi

1. Nhan vao dieu phoi can sua trong danh sach de chuyen den man hinh chi tiet.
2. Nhan nut **Sua**.
3. Thay doi gia tri cua cac muc can sua.
4. Nhan nut **Luu**.

> **Canh bao**: Khong the sua dieu phoi co trang thai **Dang thuc hien (IN_PROGRESS)** hoac **Hoan thanh (COMPLETED)**. Neu can sua, vui long lien he quan tri vien.

#### 4.2.4 Xoa dieu phoi

1. Nhan vao dieu phoi can xoa trong danh sach de chuyen den man hinh chi tiet.
2. Nhan nut **Xoa**.
3. Cua so xac nhan "Ban co chac chan muon xoa?" se hien thi.
4. Nhan **Xac nhan** de hoan thanh xoa.

> **Canh bao**: Chi co the xoa dieu phoi co trang thai **Da dang ky (REGISTERED)**. Du lieu da xoa khong the khoi phuc, vui long xu ly can than.

#### 4.2.5 Thay doi trang thai dieu phoi

Trang thai dieu phoi tu dong thay doi theo tien do can. Khi can thiet, co the thay doi thu cong.

| Trang thai hien tai | Trang thai co the chuyen | Mo ta |
|-----------|----------------|------|
| Da dang ky (REGISTERED) | Da huy (CANCELLED) | Xu ly huy dieu phoi |
| Dang thuc hien (IN_PROGRESS) | Da huy (CANCELLED) | Huy dieu phoi dang thuc hien (bat buoc nhap ly do) |

1. Nhan nut **Thay doi trang thai** tren man hinh chi tiet dieu phoi.
2. Chon trang thai muon chuyen doi.
3. Khi xu ly huy, nhap **ly do huy**.
4. Nhan nut **Xac nhan**.

> **Luu y**: Viec chuyen tu trang thai Da dang ky sang Dang thuc hien duoc he thong tu dong xu ly khi bat dau can lan 1. Viec chuyen sang trang thai Hoan thanh cung duoc tu dong xu ly khi hoan thanh lan can cuoi cung.

### 4.3 Quan ly can

Quan ly can (`/weighing`) la chuc nang xem va quan ly tat ca ban ghi can.

#### 4.3.1 Xem ban ghi can

1. Nhan **Quan ly can** o menu ben trai.
2. Danh sach ban ghi can se duoc hien thi.
3. Su dung dieu kien tim kiem o phia tren de xem.

   | Dieu kien tim kiem | Mo ta |
   |-----------|------|
   | Thoi gian | Ngay bat dau ~ ngay ket thuc theo thoi gian can |
   | Che do can | LPR (tu dong), Di dong (OTP), Thu cong (manual) |
   | Loai hang | San pham phu, chat thai, vat lieu phu, xuat kho, chung |
   | Bien so xe | Tim theo bien so xe |
   | Doanh nghiep | Tim theo ten doanh nghiep |
   | Trang thai | Dang thuc hien, hoan thanh |

4. Nhan nut **Tim kiem**.

#### 4.3.2 Thong tin chi tiet can

Nhan vao mot muc cu the trong danh sach can de xem thong tin chi tiet.

| Phan loai thong tin | Chi tiet |
|-----------|-----------|
| **Thong tin co ban** | So can, so dieu phoi, bien so xe, tai xe, doanh nghiep, loai hang |
| **Can lan 1** | Trong luong lan 1 (kg), thoi gian can, che do can (LPR/di dong/thu cong), thong tin can |
| **Can lan 2** | Trong luong lan 2 (kg), thoi gian can, che do can, thong tin can |
| **Can lan 3** | Trong luong lan 3 (kg), thoi gian can, che do can, thong tin can (neu co) |
| **Ket qua** | Trong luong tinh (|lan 1 - lan 2|), trang thai hoan thanh can |
| **Thong tin LPR** | Hinh anh bien so nhan dien, do tin cay (%), phien ban mo hinh AI |
| **Ghi chu** | Cac luu y dac biet, lich su chinh sua |

#### 4.3.3 Can thu cong

Khi ca can tu dong (LPR) va can di dong (OTP) deu khong kha dung, nhan vien phu trach co the truc tiep nhap du lieu can thu cong.

1. Nhan nut **Can thu cong** tren man hinh quan ly can.
2. Nhap cac thong tin sau:

   | Muc nhap | Bat buoc | Mo ta |
   |-----------|-----------|------|
   | Chon dieu phoi | Bat buoc | Chon tu danh sach dieu phoi dang thuc hien |
   | Lan can | Bat buoc | Chon lan 1, lan 2 hoac lan 3 |
   | Trong luong | Bat buoc | Nhap truc tiep trong luong hien thi tren bo chi thi can (kg) |
   | Can | Bat buoc | Chon can su dung |
   | Ly do | Bat buoc | Nhap ly do can thu cong |

3. Kiem tra noi dung nhap, sau do nhan nut **Luu**.

> **Canh bao**: Chi su dung can thu cong khi can tu dong/di dong khong kha dung. Ban ghi can thu cong se duoc danh dau "Thu cong" kem ly do.

#### 4.3.4 Can lai

Khi can can lai cho mot lan can da thuc hien, xu ly nhu sau:

1. Nhan nut **Can lai** tren man hinh chi tiet can.
2. Nhap ly do can lai.
3. Nhan nut **Xac nhan**.
4. Lan can do se bi dat lai, va khi xe vao lai tram can, lan can moi se duoc thuc hien.

> **Luu y**: Lich su can lai duoc ghi nhan trong he thong, du lieu can truoc do cung duoc luu giu nhu lich su.

### 4.4 Quan ly phieu can dien tu

Quan ly phieu can dien tu (`/slip`) cho phep xem tat ca phieu can dien tu da phat hanh va quan ly lich su chia se.

#### 4.4.1 Xem phieu can dien tu

1. Nhan **Phieu can dien tu** o menu ben trai.
2. Danh sach phieu can dien tu da phat hanh se duoc hien thi.
3. Su dung dieu kien tim kiem (thoi gian, bien so xe, doanh nghiep, loai hang) de xem.
4. Nhan vao phieu can cu the trong danh sach de hien thi noi dung chi tiet.

#### 4.4.2 Xem lich su chia se

1. Nhan tab **Lich su chia se** tren man hinh chi tiet phieu can dien tu.
2. Lich su chia se cua phieu can do se duoc hien thi.

   | Muc lich su | Mo ta |
   |-----------|------|
   | Thoi gian chia se | Ngay va gio chia se |
   | Phuong thuc chia se | KakaoTalk, SMS... |
   | Nguoi nhan | Thong tin doi tuong chia se |
   | Nguoi chia se | Nguoi dung thuc hien chia se |

### 4.5 Quan ly xuat cong

Quan ly xuat cong (`/gate-pass`) la chuc nang duyet hoac tu choi viec xe ra khoi nha may thep.

#### 4.5.1 Duyet xuat cong

1. Nhan **Quan ly xuat cong** o menu ben trai.
2. Danh sach yeu cau xuat cong dang cho duyet se duoc hien thi.
3. Nhan vao muc xuat cong can duyet de chuyen den man hinh chi tiet.
4. Kiem tra cac thong tin sau:

   | Muc kiem tra | Mo ta |
   |-----------|------|
   | Thong tin dieu phoi | So dieu phoi, loai hang, doanh nghiep |
   | Thong tin xe | Bien so xe, tai xe |
   | Thong tin can | Trang thai hoan thanh can, trong luong tinh |
   | Phieu can dien tu | Trang thai phat hanh phieu can |

5. Khi da kiem tra xong, nhan nut **Duyet**.
6. Thong bao xac nhan "Xuat cong da duoc duyet." se hien thi.

#### 4.5.2 Tu choi xuat cong

1. Nhan nut **Tu choi** tren man hinh chi tiet xuat cong.
2. Nhap **ly do tu choi** (bat buoc).
3. Nhan nut **Xac nhan**.
4. Thong bao tu choi se duoc gui den tai xe.

> **Canh bao**: Chi co the duyet xuat cong cho cac chuyen can da hoan thanh binh thuong va phieu can dien tu da duoc phat hanh. Doi voi cac chuyen chua hoan thanh can, nhat dinh phai xu ly tu choi.

### 4.6 Quan ly du lieu goc

Quan ly du lieu goc la chuc nang quan ly du lieu co ban can thiet cho van hanh he thong. Nhan **Du lieu goc** o menu ben trai de hien thi menu con.

#### 4.6.1 Quan ly doanh nghiep

Quan ly doanh nghiep (`/master/company`) cho phep dang ky va quan ly thong tin doi tac.

**Dang ky doanh nghiep**

1. Nhan **Du lieu goc > Doanh nghiep** o menu ben trai.
2. Nhan nut **Dang ky moi**.
3. Nhap cac thong tin sau:

   | Muc nhap | Bat buoc | Mo ta |
   |-----------|-----------|------|
   | Ma doanh nghiep | Bat buoc | Ma dinh danh doanh nghiep duy nhat |
   | Ten doanh nghiep | Bat buoc | Ten doanh nghiep |
   | Ma so thue | Bat buoc | Ma so thue doanh nghiep (10 chu so) |
   | Nguoi dai dien | Tuy chon | Ten nguoi dai dien |
   | So dien thoai | Tuy chon | So lien lac doanh nghiep |
   | Dia chi | Tuy chon | Dia chi doanh nghiep |
   | Trang thai su dung | Bat buoc | Chon su dung/khong su dung |

4. Nhan nut **Luu**.

**Xem/Sua/Xoa doanh nghiep**

- **Xem**: Tim theo ten doanh nghiep hoac ma doanh nghiep trong danh sach.
- **Sua**: Nhan vao doanh nghiep de vao man hinh chi tiet, sau do nhan nut **Sua** de thay doi thong tin.
- **Xoa**: Nhan nut **Xoa** tren man hinh chi tiet. Neu doanh nghiep co ban ghi dieu phoi/can lien ket, khong the xoa ma thay vao do xu ly **Khong su dung**.

#### 4.6.2 Quan ly xe

Quan ly xe (`/master/vehicle`) cho phep dang ky va quan ly thong tin xe can can.

**Dang ky xe**

1. Nhan **Du lieu goc > Xe** o menu ben trai.
2. Nhan nut **Dang ky moi**.
3. Nhap cac thong tin sau:

   | Muc nhap | Bat buoc | Mo ta |
   |-----------|-----------|------|
   | Bien so xe | Bat buoc | So bien so xe (vi du: 12A-34567) |
   | Loai xe | Bat buoc | Xe ben, xe tai, xe bon... |
   | Doanh nghiep truc thuoc | Bat buoc | Chon doanh nghiep da dang ky trong du lieu goc |
   | Tai xe | Tuy chon | Tai xe mac dinh duoc phan cong |
   | Trong luong xe khong | Tuy chon | Trong luong co ban cua xe rong (kg) |
   | Trang thai su dung | Bat buoc | Chon su dung/khong su dung |

4. Nhan nut **Luu**.

> **Luu y**: Bien so xe la thong tin quan trong duoc su dung de doi chieu dieu phoi khi LPR nhan dien tu dong. Vui long nhap chinh xac.

**Xem/Sua/Xoa xe**

- **Xem**: Tim theo bien so xe, ten doanh nghiep, loai xe trong danh sach xe.
- **Sua**: Nhan vao xe de vao man hinh chi tiet, sau do nhan nut **Sua**.
- **Xoa**: Nhan nut **Xoa** tren man hinh chi tiet. Neu co du lieu lien ket, khong the xoa ma xu ly **Khong su dung**.

#### 4.6.3 Quan ly can (Thiet bi can)

Quan ly can (`/master/scale`) cho phep dang ky va quan ly thong tin thiet bi can tai hien truong.

**Dang ky can**

1. Nhan **Du lieu goc > Can** o menu ben trai.
2. Nhan nut **Dang ky moi**.
3. Nhap cac thong tin sau:

   | Muc nhap | Bat buoc | Mo ta |
   |-----------|-----------|------|
   | Ma can | Bat buoc | Ma dinh danh can duy nhat |
   | Ten can | Bat buoc | Ten can (vi du: Can so 1, Can cang) |
   | Vi tri lap dat | Bat buoc | Vi tri vat ly lap dat |
   | Cong suat toi da | Bat buoc | Trong luong toi da co the can (kg) |
   | Do chia nho nhat | Bat buoc | Don vi do nho nhat (kg) |
   | Cong giao tiep | Bat buoc | Thong tin cong ket noi RS-232C |
   | Ngay hieu luc kiem dinh | Bat buoc | Ngay het han kiem dinh can |
   | Trang thai su dung | Bat buoc | Chon su dung/khong su dung |

4. Nhan nut **Luu**.

> **Canh bao**: Khi chung chi kiem dinh cua can het han, ket qua can thuc hien tren can do co the khong co hieu luc phap ly. Vui long quan ly ngay hieu luc kiem dinh can than.

#### 4.6.4 Quan ly ma chung

Quan ly ma chung (`/master/code`) cho phep quan ly cac ma phan loai su dung trong toan he thong.

1. Nhan **Du lieu goc > Ma chung** o menu ben trai.
2. Danh sach nhom ma se duoc hien thi.
3. Chon nhom ma de hien thi danh sach ma chi tiet thuoc nhom do.

Cac nhom ma chung chinh:

| Nhom ma | Mo ta | Vi du |
|-----------|------|------|
| ITEM_TYPE | Loai hang | San pham phu, chat thai, vat lieu phu, xuat kho, chung |
| WEIGH_MODE | Che do can | LPR (tu dong), di dong (OTP), thu cong (manual) |
| DISPATCH_STATUS | Trang thai dieu phoi | Da dang ky, dang thuc hien, hoan thanh, da huy |
| VEHICLE_TYPE | Loai xe | Xe ben, xe tai, xe bon... |
| GATE_STATUS | Trang thai xuat cong | Cho xu ly, da duyet, da tu choi |

> **Canh bao**: Viec thay doi ma chung anh huong den toan bo he thong. Vui long kiem tra pham vi anh huong truoc khi thay doi va tham khao y kien quan tri vien khi can thiet.

### 4.7 Thong ke

Chuc nang thong ke (`/statistics`) phan tich du lieu can theo nhieu tieu chi va cung cap duoi dang bao cao.

#### 4.7.1 Thong ke theo ngay

1. Nhan **Thong ke** o menu ben trai.
2. Chon tab **Theo ngay** o phia tren.
3. Cai dat khoang thoi gian xem (ngay bat dau ~ ngay ket thuc).
4. Neu can, chon bo loc **theo doanh nghiep van tai**, **theo loai hang** de thu hep dieu kien.
5. Nhan nut **Xem**.
6. So chuyen can, tong trong luong, ty le theo loai hang duoc hien thi bang bang va bieu do.

#### 4.7.2 Thong ke theo thang

1. Chon tab **Theo thang** o phia tren.
2. Cai dat nam va khoang thang xem.
3. Neu can, chon bo loc **theo doanh nghiep van tai**, **theo loai hang** de thu hep dieu kien.
4. Nhan nut **Xem**.
5. Bieu do xu huong theo thang va du lieu chi tiet duoc hien thi.

#### 4.7.3 Thong ke theo loai hang

1. Chon tab **Theo loai hang** o phia tren.
2. Cai dat khoang thoi gian va loai hang (co the chon nhieu).
3. Neu can, chon bo loc **theo doanh nghiep van tai** de xem thong ke loai hang cua doanh nghiep cu the.
4. Nhan nut **Xem**.
5. So chuyen can, tong trong luong, ty le theo loai hang duoc hien thi.

#### 4.7.4 Thong ke theo doanh nghiep

1. Chon tab **Theo doanh nghiep** o phia tren.
2. Cai dat khoang thoi gian va doanh nghiep (co the chon nhieu).
3. Neu can, chon bo loc **theo loai hang** de xem thong ke doanh nghiep cho loai hang cu the.
4. Nhan nut **Xem**.
5. Ket qua can theo doanh nghiep duoc hien thi.

#### 4.7.5 Tai xuong Excel

Tren tat ca man hinh thong ke, ban co the tai xuong ket qua xem duoi dang file Excel.

1. Xem thong ke mong muon.
2. Nhan nut **Tai xuong Excel** o goc tren ben phai man hinh.
3. Du lieu theo dieu kien xem hien tai se duoc tai xuong duoi dang file `.xlsx`.
4. Ten file theo dinh dang `ThongKeCan_[Loai]_[ThoiGian].xlsx`.

> **Luu y**: Khi tai xuong du lieu lon co the mat thoi gian. Vui long kiem tra tien do tai xuong tren trinh duyet.
>
> **Luu y**: Tren tat ca cac tab thong ke, ban co the ket hop bo loc **theo doanh nghiep van tai** va **theo loai hang**. Khi tai xuong Excel voi bo loc da ap dung, ket qua tai xuong se phan anh dieu kien bo loc.

### 4.8 Trang thai tram can thoi gian thuc

Man hinh trang thai tram can thoi gian thuc (`/weighing-station`) la chuc nang giam sat thoi gian thuc cac hoat dong can dang dien ra tai tram can.

#### Kiem tra trang thai tram can

1. Nhan **Trang thai tram can** trong menu con cua **Quan ly can** o menu ben trai.
2. Trang thai hien tai cua tung tram can duoc hien thi dang the.

| Muc hien thi | Mo ta |
|-----------|------|
| **Ten tram can** | Ten va vi tri tram can |
| **Trang thai hien tai** | Cho, dang can, loi... |
| **Thong tin xe** | Bien so xe va ten doanh nghiep dang can |
| **Gia tri trong luong** | Hien thi trong luong thoi gian thuc (qua WebSocket) |
| **Che do can** | LPR (tu dong), OTP (di dong), thu cong |
| **Giai doan** | Phan biet can lan 1/2/3 |

3. Khi can hoan thanh, trang thai se tu dong cap nhat.

> **Luu y**: Man hinh nay cap nhat thoi gian thuc qua WebSocket. Trang thai moi nhat duoc phan anh ma khong can lam moi rieng.

### 4.9 Giam sat (Quan ly thiet bi)

Chuc nang giam sat (`/monitoring`) cho phep kiem tra trang thai ket noi thoi gian thuc cua thiet bi tram can (tram can, camera, bo chi thi, thanh chan).

#### Bang tom tat thiet bi

Phia tren man hinh giam sat hien thi **bang tom tat thiet bi**. Ban co the nam bat tong quan trang thai toan bo thiet bi.

| Muc tom tat | Mo ta |
|-----------|------|
| **Tong so thiet bi** | Tong so thiet bi da dang ky trong he thong |
| **Truc tuyen** | So thiet bi dang hoat dong binh thuong (mau xanh) |
| **Ngoai tuyen** | So thiet bi mat ket noi (mau xam) |
| **Loi** | So thiet bi gap su co (mau do) |

#### Kiem tra trang thai thiet bi

1. Nhan **Giam sat** o menu ben trai.
2. Phia tren hien thi bang tom tat thiet bi, phia duoi hien thi danh sach thiet bi hien truong va trang thai.

| Trang thai | Hien thi | Mo ta |
|------|------|------|
| **Truc tuyen (Online)** | Mau xanh | Thiet bi dang hoat dong binh thuong |
| **Ngoai tuyen (Offline)** | Mau xam | Mat ket noi voi thiet bi |
| **Loi (Error)** | Mau do | Thiet bi gap su co |

Cac thiet bi duoc giam sat:

| Loai thiet bi | Mo ta |
|-----------|------|
| **Bo chi thi can** | Thiet bi doc du lieu trong luong qua giao tiep RS-232C |
| **Camera LPR** | Camera chup bien so xe |
| **Cam bien LiDAR** | Cam bien phat hien xe vao |
| **Bang dien tu LED** | Thiet bi hien thi thong bao huong dan cho tai xe |
| **Thanh chan** | Thanh chan cua ra/vao tram can |

3. Nhan vao thiet bi cu the de xem thong tin trang thai chi tiet.

   | Thong tin chi tiet | Mo ta |
   |-----------|------|
   | Ten thiet bi | Ten va ma thiet bi |
   | Vi tri lap dat | Vi tri vat ly |
   | Trang thai hien tai | Truc tuyen/Ngoai tuyen/Loi |
   | Lan giao tiep cuoi | Thoi diem giao tiep thanh cong lan cuoi |
   | Noi dung loi | Noi dung loi (neu o trang thai loi) |

> **Canh bao**: Khi trang thai thiet bi la **Ngoai tuyen** hoac **Loi**, can tu dong tai tram can do co the khong hoat dong binh thuong. Hay lien he ngay quan tri vien hoac nhan vien phu trach thiet bi.

> **Luu y**: Man hinh giam sat cap nhat thoi gian thuc qua WebSocket. Trang thai moi nhat duoc phan anh ma khong can lam moi rieng.

---

## 5. Chuc nang chung

Day la cac chuc nang chung ma ca tai xe va nhan vien phu trach deu co the su dung.

### 5.1 Trang ca nhan

Trang ca nhan (`/mypage`) cho phep xem va sua thong tin ho so ca nhan, thay doi mat khau, va cai dat phuong thuc nhan thong bao.

#### 5.1.1 Xem thong tin ho so

1. Web: Nhan **bieu tuong nguoi dung** goc tren ben phai, sau do chon **Trang ca nhan**
   Di dong: Chon tab **Ca nhan** o thanh dieu huong duoi
2. Thong tin ho so ca nhan se duoc hien thi.

   | Muc | Mo ta |
   |------|------|
   | ID nguoi dung | ID dang nhap (khong the thay doi) |
   | Ho ten | Ten nguoi dung |
   | So dien thoai | So dien thoai da dang ky |
   | Email | Dia chi email da dang ky |
   | Don vi | Doanh nghiep hoac bo phan truc thuoc |
   | Vai tro | Quan tri vien, nhan vien phu trach, tai xe |

#### 5.1.2 Sua thong tin ho so

Ban co the sua thong tin ca nhan nhu ho ten, so dien thoai, email tai trang ca nhan.

1. Nhan nut **Sua ho so** tren man hinh trang ca nhan.
2. Thay doi gia tri cua cac muc can sua.

   | Muc | Co the sua | Mo ta |
   |------|----------------|------|
   | ID nguoi dung | Khong the thay doi | ID dang nhap khong the thay doi |
   | Ho ten | Co the sua | Sua ten nguoi dung |
   | So dien thoai | Co the sua | Thay doi so lien lac |
   | Email | Co the sua | Thay doi dia chi email |
   | Don vi | Khong the thay doi | Yeu cau quan tri vien thay doi don vi |
   | Vai tro | Khong the thay doi | Yeu cau quan tri vien thay doi vai tro |

3. Kiem tra noi dung da sua, sau do nhan nut **Luu**.
4. Khi hien thi thong bao "Ho so da duoc sua.", viec thay doi hoan tat.

#### 5.1.3 Cai dat thong bao

Ban co the cai dat nhan thong bao va phuong thuc nhan tai trang ca nhan.

1. Kiem tra muc **Cai dat thong bao** tren man hinh trang ca nhan.
2. Co the bat hoac tat theo tung phuong thuc nhan:

   | Phuong thuc nhan | Mo ta |
   |-----------|------|
   | **Thong bao day** | Nhan thong bao day thoi gian thuc qua ung dung di dong va trinh duyet |
   | **Thong bao email** | Nhan thong bao qua dia chi email da dang ky |

3. Ngoai ra, co the cai dat nhan theo tung loai thong bao:

   | Loai thong bao | Mo ta |
   |-----------|------|
   | Thong bao dieu phoi | Nhan thong bao khi co dieu phoi moi |
   | Hoan thanh can | Nhan thong bao khi can hoan thanh |
   | Phat hanh phieu can | Nhan thong bao khi phieu can dien tu duoc tao |
   | Thong bao chung | Nhan thong bao khi co thong bao moi |
   | Thong bao he thong | Nhan thong bao lien quan tai khoan, bao tri he thong... |

4. Bat hoac tat cong tac cua thong bao mong muon.
5. Thay doi duoc luu ngay lap tuc.

> **Luu y**: De nhan thong bao day tren ung dung di dong, quyen thong bao cua ung dung phai duoc cho phep trong cai dat dien thoai. Token thong bao day duoc tu dong dang ky khi khoi dong ung dung lan dau.
>
> **Luu y**: De nhan thong bao email, dia chi email chinh xac phai duoc dang ky trong trang ca nhan.

#### 5.1.4 Thay doi mat khau

1. Nhan nut **Thay doi mat khau** tren man hinh trang ca nhan.
2. Nhap cac thong tin sau:

   | Muc nhap | Mo ta |
   |-----------|------|
   | Mat khau hien tai | Nhap mat khau dang su dung |
   | Mat khau moi | Nhap mat khau muon thay doi |
   | Xac nhan mat khau moi | Nhap lai mat khau moi |

3. Nhan nut **Thay doi**.
4. Khi hien thi thong bao "Mat khau da duoc thay doi.", viec thay doi hoan tat.

Quy tac mat khau:

| Quy tac | Mo ta |
|------|------|
| Do dai toi thieu | Tu 8 ky tu tro len |
| To hop ky tu | Ket hop it nhat 3 trong so: chu hoa, chu thuong, so, ky tu dac biet |
| Mat khau truoc | Khong the su dung lai 3 mat khau gan nhat |
| Chu ky thay doi | Khuyen nghi thay doi moi 90 ngay (thong bao truoc 7 ngay khi het han) |

> **Canh bao**: Khi thay doi mat khau, phien lam viec hien tai se ket thuc. Vui long dang nhap lai bang mat khau moi.

### 5.2 Thong bao

Chuc nang thong bao cho phep xem cac huong dan quan trong tu he thong.

#### Cach xem thong bao

**Web**:
1. Nhan vao **bieu tuong thong bao** (hinh chuong) tren thanh tieu de.
2. So thong bao chua doc duoc hien thi dang huy hieu tren bieu tuong.
3. Danh sach thong bao hien thi dang menu xo xuong.
4. Nhan vao thong bao de chuyen den man hinh tuong ung.

**Di dong**:
1. Nhan **bieu tuong thong bao** tren thanh tren.
2. Chuyen den man hinh danh sach thong bao.
3. Nhan vao thong bao de chuyen den man hinh chi tiet tuong ung.

> **Luu y**: Doi voi ung dung di dong, ban co the nhan thong bao quan trong qua thong bao day ngay ca khi ung dung chay nen. Vui long cho phep thong bao ung dung trong cai dat dien thoai.

### 5.3 Thong bao chung

Thong bao chung (`/notice`) cho phep xem cac thong tin lien quan he thong, lich bao tri, thay doi van hanh...

1. Web: Nhan **Thong bao** o menu ben trai
   Di dong: Truy cap tu banner **Thong bao** tren man hinh chinh hoac menu
2. Danh sach thong bao hien thi theo thu tu ngay. Thong bao **ghim (📌)** luon hien thi o dau danh sach.
3. Su dung bo loc **theo danh muc** de xem chi loai thong bao mong muon.
4. Nhap tu khoa vao **o tim kiem** o phia tren de tim thong bao.
5. Nhan vao thong bao de hien thi noi dung chi tiet.
6. Neu co file dinh kem, ban co the tai xuong.

### 5.4 Tro giup/FAQ

Tro giup (`/help`) cho phep xem cac cau hoi thuong gap va tra loi ve cach su dung he thong.

1. Web: Nhan **Tro giup** o menu ben trai
   Di dong: Tab **Tro giup** tai trang ca nhan
2. Danh sach FAQ duoc phan loai theo danh muc.
3. Nhan vao cau hoi de mo rong cau tra loi.
4. Nhap tu khoa vao o tim kiem o phia tren de tim cau hoi lien quan.

---

## 6. Cau hoi thuong gap (FAQ)

### Dang nhap/Tai khoan

**Q1. Toi quen mat khau. Phai lam sao?**

A. Nhan **Quen mat khau** tren man hinh dang nhap de nhan mat khau tam thoi qua email hoac so dien thoai da dang ky. Sau khi dang nhap bang mat khau tam thoi, vui long thay doi mat khau tai trang ca nhan. Neu khong tu giai quyet duoc, hay yeu cau quan tri vien dat lai mat khau.

**Q2. Hien thi thong bao tai khoan bi khoa.**

A. Tai khoan se bi khoa trong 30 phut khi nhap sai mat khau 5 lan lien tiep. Vui long thu lai sau 30 phut voi mat khau dung. Trong truong hop khan cap, yeu cau quan tri vien mo khoa ngay.

**Q3. Khong the tu dong dang nhap tren ung dung di dong.**

A. Tu dong dang nhap co hieu luc trong 7 ngay sau lan dang nhap cuoi. Sau 7 ngay, ban can dang nhap lai. Khi xoa va cai dat lai ung dung cung can dang nhap lai.

### Dieu phoi

**Q4. Dieu phoi cua toi khong hien thi trong danh sach.**

A. Vui long kiem tra cac muc sau:
- Xac nhan tai khoan dang nhap co dung la tai khoan cua tai xe duoc phan cong cho dieu phoi do khong.
- Kiem tra bo loc (trang thai, ngay) cua danh sach dieu phoi da duoc cai dat dung chua.
- Dieu phoi co the da bi huy (CANCELLED), hay xem bao gom ca trang thai da huy.
- Neu van de van tiep tuc, vui long lien he nhan vien phu trach can.

**Q5. Trang thai dieu phoi khong thay doi.**

A. Trang thai dieu phoi duoc he thong tu dong thay doi theo tien do can. Khi bat dau can lan 1, tu dong chuyen sang "Dang thuc hien", khi hoan thanh lan can cuoi cung tu dong chuyen sang "Hoan thanh". Neu can thay doi thu cong, nhan vien phu trach co the thay doi trang thai tai man hinh chi tiet dieu phoi.

### Can

**Q6. Xe da vao tram can nhung khong duoc nhan dien tu dong.**

A. Vui long kiem tra cac muc sau:
1. Kiem tra bien so xe co sach va khong bi che khuat khong.
2. Kiem tra xe da dung chinh xac tren tram can chua.
3. Kiem tra thong bao huong dan tren bang dien tu LED.
4. Neu LPR khong nhan dien duoc va bang dien tu LED hien thi ma OTP, hay tien hanh can bang OTP tren di dong.
5. Neu can OTP cung khong kha dung, lien he phong dieu do de yeu cau can thu cong.

**Q7. Nhap ma OTP nhung hien thi "Ma khong hop le".**

A. Vui long kiem tra cac muc sau:
- Kiem tra da nhap chinh xac ma hien thi tren bang dien tu LED (6 chu so).
- OTP het han sau 5 phut ke tu khi cap. Vui long kiem tra OTP moi.
- Sau 3 lan nhap sai lien tiep, OTP do se bi vo hieu hoa. Vui long lien he phong dieu do.

**Q8. Trong luong tinh co ve khong dung.**

A. Trong luong tinh duoc tu dong tinh theo cong thuc |Trong luong lan 1 - Trong luong lan 2|. Vui long kiem tra trong luong tung lan tai man hinh chi tiet can. Neu ban cho rang trong luong co van de, hay yeu cau nhan vien phu trach can lai.

### Phieu can dien tu

**Q9. Phieu can dien tu khong duoc tao.**

A. Phieu can dien tu tu dong duoc tao sau khi hoan thanh lan can cuoi cung (thuong la can lan 2). Phieu can khong duoc phat hanh khi can con dang thuc hien. Neu tat ca cac lan can da hoan thanh ma van khong co phieu can, vui long lien he nhan vien phu trach can.

**Q10. Chia se phieu can dien tu qua KakaoTalk nhung doi phuong khong the xem.**

A. Lien ket chia se can ket noi internet. Vui long kiem tra nguoi nhan co dang ket noi internet khi nhan vao lien ket. Neu van de van tiep tuc, hay thu chia se lai qua SMS.

### Xuat cong

**Q11. Xuat cong bi tu choi. Phai lam sao?**

A. Vui long kiem tra ly do tu choi trong thong bao tu choi xuat cong. Tuy theo ly do nhu can chua hoan thanh, thieu ho so..., hoan thanh cac thu tuc can thiet roi yeu cau xuat cong lai hoac lien he nhan vien phu trach can.

### Khac

**Q12. Khong nhan duoc thong bao tren ung dung di dong.**

A. Vui long kiem tra quyen thong bao cua ung dung Can thong minh Busan da duoc cho phep trong cai dat dien thoai. Voi Android, kiem tra tai "Cai dat > Ung dung > Can thong minh Busan > Thong bao". Voi iOS, kiem tra tai "Cai dat > Thong bao > Can thong minh Busan".

---

## 7. Huong dan xu ly su co

Phan nay huong dan cac tinh huong su co chinh co the xay ra trong qua trinh su dung he thong va cach giai quyet.

### 7.1 Su co lien quan dang nhap

| Trieu chung | Nguyen nhan | Cach giai quyet |
|------|------|-----------|
| "ID hoac mat khau khong khop" | Nhap sai ID/mat khau | Kiem tra Caps Lock roi nhap lai. Su dung chuc nang quen mat khau |
| "Tai khoan da bi khoa" | Nhap sai mat khau 5 lan lien tiep | Cho 30 phut roi thu lai hoac yeu cau quan tri vien mo khoa |
| Dang xuat ngay sau khi dang nhap | Loi cap access token | Xoa cookie/cache trinh duyet roi thu lai |
| "Phien lam viec da het han" | Khong su dung qua 30 phut | Dang nhap lai (khi tu dong lam moi trong 7 ngay that bai) |
| Dang nhap that bai tren ung dung di dong | Ket noi mang kem | Kiem tra ket noi Wi-Fi hoac du lieu di dong roi thu lai |

### 7.2 Su co lien quan can

| Trieu chung | Nguyen nhan | Cach giai quyet |
|------|------|-----------|
| LPR khong nhan dien duoc | Bien so ban/hong/bi che | Chuyen sang can OTP tren di dong. Lam sach bien so roi thu lai |
| Nhap OTP that bai | Ma het han hoac nhap sai | Kiem tra OTP moi trong vong 5 phut roi nhap lai. Sau 3 lan that bai, lien he phong dieu do |
| Trong luong hien thi 0 | Loi giao tiep bo chi thi can | Yeu cau phong dieu do kiem tra thiet bi. Chuyen sang can thu cong |
| "Khong tim thay dieu phoi" | Xe chua dang ky/khong co dieu phoi | Kiem tra voi nhan vien phu trach ve viec dang ky dieu phoi. Kiem tra dang ky bien so xe |
| Du lieu can khong luu duoc | Loi giao tiep may chu | Kiem tra ket noi mang. Thu lai. Neu van tiep tuc, lien he phong dieu do |
| "Dieu phoi khac dang thuc hien" | Trung lap dieu phoi cung xe | Hoan thanh hoac huy dieu phoi truoc roi thu lai |

### 7.3 Su co lien quan phieu can dien tu

| Trieu chung | Nguyen nhan | Cach giai quyet |
|------|------|-----------|
| Phieu can khong duoc tao | Chua hoan thanh can | Kiem tra tat ca cac lan can da hoan thanh chua |
| Noi dung phieu can sai | Du lieu nhap sai | Yeu cau nhan vien phu trach sua |
| Chia se qua KakaoTalk that bai | Chua cai dat KakaoTalk/thieu quyen | Kiem tra cai dat ung dung KakaoTalk. Thay the bang chia se SMS |
| Khong truy cap duoc lien ket chia se | Loi mang | Kiem tra ket noi internet. Thu lai sau |

### 7.4 Su co lien quan ung dung di dong

| Trieu chung | Nguyen nhan | Cach giai quyet |
|------|------|-----------|
| Ung dung khong khoi dong duoc | Phien ban OS khong du | Kiem tra Android 10+ hoac iOS 14+. Cap nhat ung dung |
| Ung dung bi treo | Thieu bo nho | Dong ung dung cuong che roi khoi dong lai. Khoi dong lai thiet bi |
| Tai du lieu that bai | Mang khong on dinh | Kiem tra ket noi Wi-Fi hoac du lieu. Thu lai trong moi truong on dinh |
| Khong nhan thong bao | Chua cap quyen thong bao | Cho phep quyen thong bao ung dung trong cai dat thiet bi |
| Man hinh hien thi bat thuong | Phien ban ung dung cu | Cap nhat len phien ban moi nhat tu cua hang ung dung |

### 7.5 Su co lien quan web

| Trieu chung | Nguyen nhan | Cach giai quyet |
|------|------|-----------|
| Man hinh bi vo | Trinh duyet khong ho tro | Khuyen nghi su dung Chrome 90 tro len |
| Du lieu thoi gian thuc khong cap nhat | Mat ket noi WebSocket | Lam moi trinh duyet (F5). Neu van tiep tuc, lien he quan tri vien |
| Tai xuong Excel that bai | Chan pop-up | Kiem tra tat chan pop-up cua trinh duyet |
| Trang tai cham | Truy van du lieu lon | Thu hep dieu kien tim kiem (thoi gian) roi truy van lai |
| "Ban khong co quyen" | Thieu quyen vai tro | Kiem tra voi quan tri vien ve quyen truy cap chuc nang do |

### 7.6 Su co lien quan giam sat thiet bi

| Trieu chung | Nguyen nhan | Cach giai quyet |
|------|------|-----------|
| Trang thai thiet bi "Ngoai tuyen" | Su co giao tiep | Yeu cau quan tri vien hoac nhan vien phu trach thiet bi kiem tra |
| Trang thai thiet bi "Loi" | Thiet bi gap su co | Kiem tra noi dung loi roi yeu cau nhan vien phu trach thiet bi sua chua |
| LiDAR khong phat hien | Cam bien loi | Lien he phong dieu do. Chuyen sang can thu cong |
| Bang dien tu LED hien thi bat thuong | Loi giao tiep | Lien he phong dieu do. Lam theo huong dan bang mieng |
| Thanh chan khong hoat dong | Su co co khi | Lien he ngay nhan vien an toan tai hien truong |

### 7.7 So dien thoai lien he khan cap

| Phan loai | So lien lac | Nghiep vu phu trach |
|------|--------|-----------|
| **Phong dieu do** | Kiem tra so noi bo | Van hanh can, loi OTP, su co thiet bi |
| **Kho vat tu** | Kiem tra so noi bo | Hang hoa, xep/do hang |
| **Quan tri he thong** | Kiem tra so noi bo | Quan ly tai khoan, loi he thong |
| **Nhan vien phu trach thiet bi** | Kiem tra so noi bo | Sua chua thiet bi, kiem tra phan cung |

> **Luu y**: So lien lac cu the co the xem tai bang tin hien truong hoac trong muc thong bao.

---

## 8. Thuat ngu

Sau day la cac thuat ngu chinh su dung trong he thong, sap xep theo thu tu ABC.

| Thuat ngu | Tieng Anh | Mo ta |
|------|------|------|
| **Access Token** | Access Token | Khoa tam thoi dung de xac thuc khi gui yeu cau den may chu sau khi dang nhap. Thoi han 30 phut |
| **Bien so xe** | License Plate | Bien so dang ky tren xe, duoc su dung cho nhan dien tu dong |
| **Bo chi thi can (Indicator)** | Weighing Indicator | Thiet bi hien thi va truyen du lieu trong luong do duoc tu tram can (load cell) |
| **Chat thai** | Waste | Chat thai phat sinh tu qua trinh luyen thep |
| **Dieu phoi** | Dispatch | Viec phan cong chuyen van tai cho xe |
| **Du lieu goc** | Master Data | Du lieu co ban lam nen tang van hanh he thong: doanh nghiep, xe, can, ma chung... |
| **Giay xuat cong (Gate Pass)** | Gate Pass | Giay phep can thiet khi xe roi khoi nha may thep. Can su duyet cua nhan vien phu trach |
| **JWT** | JSON Web Token | Chuan token dung cho xac thuc nguoi dung. Gom access token (30 phut) va refresh token (7 ngay) |
| **Lan can** | Measurement Order | Thu tu can: lan 1 (do dau tien), lan 2 (do thu hai), lan 3 (do thu ba) |
| **LED (Bang dien tu)** | LED Display Board | Thiet bi hien thi dien tu tai hien truong tram can, hien thi thong bao huong dan cho tai xe |
| **LiDAR** | Light Detection and Ranging | Cam bien su dung laser de do khoang cach den vat the. Dung de phat hien xe vao |
| **Load Cell** | Load Cell | Cam bien chuyen doi trong luong thanh tin hieu dien. Duoc tich hop trong tram can |
| **LPR** | License Plate Recognition | Cong nghe tu dong nhan dien bien so xe. Camera chup anh va AI phan tich de doc bien so |
| **OTP** | One-Time Password | Mat khau su dung mot lan. Gom 6 chu so, co hieu luc trong 5 phut |
| **Phieu can dien tu** | Electronic Weighing Slip (e-Slip) | Ket qua can phat hanh dang tai lieu dien tu. Thay the phieu can giay |
| **RS-232C** | RS-232C | Chuan giao tiep noi tiep giua bo chi thi can va may chu. Dung de truyen du lieu trong luong |
| **San pham phu** | Byproduct | Chat lieu phat sinh phu ngoai san pham chinh trong qua trinh luyen thep (xi, bui...) |
| **Bang dieu khien** | Dashboard | Man hinh tong hop cho phep xem cac thong tin hien trang chinh trong mot nhin |
| **Trong luong tinh** | Net Weight | Trong luong thuc cua hang hoa. Tinh bang |Trong luong lan 1 - Trong luong lan 2| |
| **Trong luong thuc (xe co hang)** | Gross Weight | Tong trong luong cua xe khi da xep hang |
| **Trong luong xe khong** | Tare Weight | Trong luong cua xe rong khong xep hang |
| **Vat lieu phu** | Sub-material | Nguyen lieu phu duoc su dung bo sung trong qua trinh luyen thep |
| **WebSocket** | WebSocket | Giao thuc ho tro giao tiep hai chieu thoi gian thuc giua may chu va may khach |
| **Xuat kho** | Export | Viec xuat vat tu tu ben trong nha may thep ra ben ngoai |

---

**Lich su tai lieu**

| Phien ban | Ngay | Tac gia | Noi dung thay doi |
|------|------|--------|-----------|
| 1.0 | 2026-01-29 | Nhom quan tri he thong | Soan ban dau |
| 1.1 | 2026-01-29 | Nhom quan tri he thong | Them chuc nang yeu thich, phan anh phuong thuc ghi lich su cuoc goi lien he, them xem lich su can, them cai dat thong bao trang ca nhan, them thong ke theo doanh nghiep tren bang dieu khien, them trang trang thai tram can thoi gian thuc, phan anh chuc nang danh muc/tim kiem/ghim thong bao, huong dan ho tro giao dien toi/sang |
| 1.2 | 2026-01-29 | Nhom quan tri he thong | Phan anh tinh nang moi: huong dan gioi thieu, phim tat, chi tiet man hinh dieu khien tram can, giam sat thiet bi, trang truy van can, trang thong ke/bao cao, trang huong dan su dung, bo nho cache ngoai tuyen di dong |

---

## Phu luc B: Huong dan cac tinh nang moi (v1.2)

### B.1 Huong dan gioi thieu (Onboarding)

Khi truy cap he thong lan dau, **huong dan gioi thieu** se tu dong hien thi. He thong se huong dan tung buoc cac menu va chuc nang chinh.

| Muc | Mo ta |
|------|------|
| **Thoi diem hien thi** | Khi dang nhap lan dau hoac chay lai tu muc tro giup |
| **Noi dung** | Cach su dung menu thanh ben, dieu huong tab, tim kiem, yeu thich |
| **Bo qua** | Co the ket thuc bat ky luc nao bang nut "Bo qua" |

### B.2 Phim tat

He thong web ho tro phim tat.

| Phim tat | Chuc nang |
|--------|------|
| `Ctrl + N` | Dang ky moi |
| `Ctrl + F` | Focus tim kiem |
| `Escape` | Dong modal/pop-up |

### B.3 Trang truy van can

Menu **Truy van can** cho phep tim kiem ket qua can truoc day voi cac dieu kien chi tiet.

| Dieu kien tim kiem | Mo ta |
|-----------|------|
| Thoi gian | Ngay bat dau ~ ngay ket thuc |
| Bien so xe | Tim kiem trung khop mot phan |
| Loai hang | San pham phu/Chat thai/Vat lieu phu/Xuat kho/Chung |
| Phuong thuc can | LPR tu dong/OTP di dong/Thu cong |
| Trang thai can | Dang thuc hien/Hoan thanh/Da huy |

> **Tai xuong Excel**: Co the tai xuong ket qua tim kiem duoi dang file Excel.

### B.4 Trang thong ke/Bao cao

Menu **Thong ke** cho phep xem cac bieu do va bao cao da dang.

| Loai thong ke | Mo ta |
|-----------|------|
| Xu huong can theo ngay | Bieu do duong hien thi so chuyen can/trong luong theo ngay |
| Ty le theo loai hang | Bieu do tron hien thi ty le can theo loai hang |
| Ket qua theo doanh nghiep | Bieu do cot hien thi ket qua can theo doanh nghiep van tai |
| Thong ke theo phuong thuc can | Ty le LPR tu dong/OTP/Thu cong |

### B.5 Trang giam sat thiet bi

Menu **Giam sat thiet bi** cho phep giam sat thoi gian thuc trang thai cac thiet bi ket noi voi tram can.

| Thiet bi | Thong tin hien thi |
|------|----------|
| Bo chi thi can | Trang thai ket noi, gia tri trong luong hien tai |
| Camera LPR | Trang thai ket noi, thoi gian chup anh gan nhat |
| Bang dien tu | Trang thai ket noi, thong bao dang hien thi |
| Thanh chan | Trang thai ket noi, trang thai dong/mo |

### B.6 Ho tro ngoai tuyen di dong

Ung dung di dong cho phep xem thong tin dieu phoi va lich su can da truy van truoc do ngay ca khi mang khong on dinh.

| Muc | Mo ta |
|------|------|
| **Doi tuong cache** | Danh sach dieu phoi, lich su can, phieu can dien tu |
| **Thoi gian hieu luc cache** | Toi da 1 gio (tu dong thu lam moi sau 1 gio) |
| **Han che** | Khong the dang ky dieu phoi moi/thuc hien can khi ngoai tuyen |

---

*Moi thac mac ve tai lieu nay, vui long lien he quan tri he thong.*
