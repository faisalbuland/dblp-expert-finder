-- leaf nodes
select c1.* from category c1 where not exists (
-- a node who calls the row in c1 its parent 
select 1 from category c2 where c2.parent_cat_key=c1.cat_key
);


select count(*) as cnt, keyword, cat_key
from pubcontent_cats pcc
join pubcontent_kws pck on pcc.publicationcontent_id = pck.publicationcontent_id 
join (
select c1.* from category c1 where not exists (
--a node who calls the row in c1 its parent 
select 1 from category c2 where c2.parent_cat_key=c1.cat_key
)
--and cat_key like 'H.2.8%'
) tmp1 on pcc.categories_cat_key=tmp1.cat_key
join keyword k on pck.keywords_id=k.id
where lower(keyword) = 'data mining'
group by keyword, cat_key having count(*) > 4
order by cnt asc;
