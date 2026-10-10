begin;

create table if not exists public.secure_wallet_profiles(
  user_id uuid primary key references auth.users(id) on delete cascade,
  vault_salt text not null,
  updated_at timestamptz not null default now()
);
alter table public.secure_wallet_profiles enable row level security;
drop policy if exists "wallet profile owner" on public.secure_wallet_profiles;
create policy "wallet profile owner" on public.secure_wallet_profiles for all to authenticated
 using(user_id=auth.uid()) with check(user_id=auth.uid());
grant select,insert,update,delete on public.secure_wallet_profiles to authenticated;

insert into storage.buckets(id,name,public,file_size_limit,allowed_mime_types)
values('secure-document-wallet','secure-document-wallet',false,26214400,array['application/octet-stream'])
on conflict(id) do update set public=false,file_size_limit=26214400,allowed_mime_types=array['application/octet-stream'];

drop policy if exists "wallet objects read" on storage.objects;
drop policy if exists "wallet objects insert" on storage.objects;
drop policy if exists "wallet objects update" on storage.objects;
drop policy if exists "wallet objects delete" on storage.objects;
create policy "wallet objects read" on storage.objects for select to authenticated
 using(bucket_id='secure-document-wallet' and (storage.foldername(name))[1]=auth.uid()::text);
create policy "wallet objects insert" on storage.objects for insert to authenticated
 with check(bucket_id='secure-document-wallet' and (storage.foldername(name))[1]=auth.uid()::text);
create policy "wallet objects update" on storage.objects for update to authenticated
 using(bucket_id='secure-document-wallet' and (storage.foldername(name))[1]=auth.uid()::text)
 with check(bucket_id='secure-document-wallet' and (storage.foldername(name))[1]=auth.uid()::text);
create policy "wallet objects delete" on storage.objects for delete to authenticated
 using(bucket_id='secure-document-wallet' and (storage.foldername(name))[1]=auth.uid()::text);

commit;
