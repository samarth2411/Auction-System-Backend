package dao;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import dto.ProductDto;
import models.Bidder;
import models.Product;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProductDao {

    @Inject
    Provider<EntityManager> entityManagerProvider;
    @Inject
    BidderDao bidderDao;
    @Transactional
    public Product AddProduct(ProductDto productDto) throws Exception{

        System.out.println("AddProduct Method Running");
        try{
            EntityManager entityManager = entityManagerProvider.get();
            Product product = new Product();
            product.setName(productDto.getName());
            product.setDescription(productDto.getDescription());
            product.setOwner(productDto.getOwner());
            if(productDto.getWinner()!=null){
                product.setWinner(productDto.getWinner());
            }
            product.setInAuction(productDto.isInAuction());
//            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            product.setStartTime(productDto.getStartTime());
            product.setEndTime(productDto.getEndTime());
            product.setPrice(productDto.getPrice());
            product.setActive(false);
            product.setRejected(false);
            entityManager.persist(product);
            System.out.println("Product Created");

            return product;

        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }

    }

    @Transactional
    public  Long count(){
        EntityManager em = entityManagerProvider.get();
        return em.createNamedQuery("ProductEntity.CountByProductNotInAuction",Long.class).getSingleResult();

    }

    @Transactional
    public List<Product> returnNotInAuctionProducts(){
        EntityManager em = entityManagerProvider.get();
        Query namedQuery = em.createNamedQuery("ProductEntity.GetByProductNotInAuction");
        return namedQuery.getResultList();
    }
    @Transactional
    public Long winCount(String userId){
        EntityManager em = entityManagerProvider.get();
        return em.createNamedQuery("Product.WinCountByUserId",Long.class).setParameter("userId",userId).getSingleResult();
    }
    @Transactional
    public Long lossCount(String userId){
        EntityManager em = entityManagerProvider.get();
        return em.createNamedQuery("Product.LostCountByUserId",Long.class).setParameter("userId",userId).getSingleResult();
    }
    @Transactional
    public List<Product> showAllInAuctionProduct(){
        EntityManager em = entityManagerProvider.get();
        Query namedQuery = em.createNamedQuery("ProductEntity.GetByProductInAuction");
        return namedQuery.getResultList();
    }
    @Transactional
    public Product showProductWithId(Long id) throws Exception{
        try{
            EntityManager em = entityManagerProvider.get();
            Query namedQuery = em.createNamedQuery("ProductEntity.GetByProductId").setParameter("id",id);
            Product singleProduct = (Product) namedQuery.getSingleResult();
            return singleProduct;

        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }


    }
    @Transactional
    public List<Product> showAllProduct(String owner){
        EntityManager em = entityManagerProvider.get();
        Query namedQuery = em.createNamedQuery("Product.getAllProductsByEmail").setParameter("owner",owner);
        List<Product> products = namedQuery.getResultList();
        return products;

    }

    @Transactional
    public Long showAllIsActiveProduct(){
        EntityManager em = entityManagerProvider.get();
        return em.createNamedQuery("ProductEntity.GetByProductIsActive",Long.class).getSingleResult();
    }

    @Transactional
    public Product isRejected(Long productId){
        try {
            EntityManager em = entityManagerProvider.get();
            TypedQuery<Product> q = em.createQuery("select p from Product p where p.id=:productId", Product.class);
            List<Product> p = q.setParameter("productId", productId).getResultList();
            if (p.size() != 0) {
                p.get(0).setRejected(true);
                return p.get(0);
            } else {
                System.out.println("Product does not exist");
            }
            return null;
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }

    }
    @Transactional
    public String inAuction(Long productId){
        try{
            EntityManager em = entityManagerProvider.get();
            Query q = em.createQuery("update Product p set p.inAuction=true where p.id=:productId");
            int count = q.setParameter("productId", productId).executeUpdate();
            if(count != 0){
                // p.get(0).setInAuction(true);
                return "Product Updates";
            }
            else{
                System.out.println("Product does not exist");
            }
            return null;
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void setWinner(String winner,Long productId){
        try{
            EntityManager em = entityManagerProvider.get();
            Query q = em.createQuery("update Product p set p.winner=:winner where p.id=:productId");
            q.setParameter("productId",productId).setParameter("winner",winner).executeUpdate();
            System.out.println("The winner of the product "+productId+" is "+ winner);
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }

    }
    @Transactional
    public void pickWinner(){
        try{
            EntityManager em = entityManagerProvider.get();
            Long date = System.currentTimeMillis();
            Query namedQuery = em.createNamedQuery("ProductEntity.GetProductList", Product.class).setParameter("time",date);
            List<Product> list = namedQuery.getResultList();
            for (Product product : list) {
                Bidder bidder = bidderDao.findBidderWithMaxBid(product.getId());
                if(bidder!=null) {
                    product.setWinner(bidder.getUserId());
                }
                product.setActive(false);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void startBid(){
        try{
            EntityManager em = entityManagerProvider.get();
            Long date = System.currentTimeMillis();
            Query namedQuery = em.createNamedQuery("ProductEntity.setIsActive").setParameter("time",date);
            List<Product> list = namedQuery.getResultList();
            for (Product product : list) {
                product.setActive(true);
            }

        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}
