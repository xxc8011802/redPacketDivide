import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class redPacketDivide
{
    public static void main(String[] args)
    {

        System.out.println(new BigDecimal(1).doubleValue());
        //发放总金额
        Integer chargeNum= 10000;
        //发放人数
        Integer sendNum=10;
        //单个红包最小金额
        Integer chargeNumMin=100;

        //转化为分
        double dChargeNum = new BigDecimal(chargeNum).divide(new BigDecimal(100)).doubleValue();
        double dChageMin = new BigDecimal(chargeNumMin).divide(new BigDecimal(100)).doubleValue();
        List<BigDecimal> allRedPacket = generateAllRedPacket(dChargeNum,
            sendNum,
            3d,
            1d,
            dChageMin);

        int sum = 0;
        for (int i = 0; i < sendNum; i++)
        {
            // 保留两位后四舍五入 , 元转换为分
            int receiveNum = (int) ((allRedPacket.get(i).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()) * 100);
            System.out.println(receiveNum);
            // BigDecimal精度丢失，最后一次取余值
            //bookRedPacketDetailDO.setReceiveNum(i == sendNum - 1 ? (chargeNum - sum) : receiveNum);
            sum += receiveNum;
            //detailDOList.add(bookRedPacketDetailDO);
        }
    }



    public static List<BigDecimal> generateAllRedPacket(double amountValue, int sizeValue, double maxMutValue, double sigmaValue, double minNum)
    {
        BigDecimal amount = new BigDecimal(amountValue);
        BigDecimal restAmount = amount;
        BigDecimal size = new BigDecimal(sizeValue);
        BigDecimal mu = restAmount.divide(size, 2, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal avg = new BigDecimal(mu.toString());
        BigDecimal maxMut = new BigDecimal(String.valueOf(maxMutValue));
        double sigma = sigmaValue <= 0 ? 1 : sigmaValue;
        List<BigDecimal> redPacketPool = new ArrayList<BigDecimal>(size.intValue());
        do {
            //当最后一个红包大于正态分布值,重新赋初值
            if (redPacketPool.size() > 0)
            {
                restAmount = amount;
                size = new BigDecimal(String.valueOf(sizeValue));
                mu = restAmount.divide(size, 2, BigDecimal.ROUND_HALF_DOWN);
                redPacketPool.clear();
            }
            int hotPacketSize = size.intValue() - 1;
            //随机出前size-1个红包，最后一个红包取剩余值，并且最后一个红包不能过大，有均值的限定倍数
            for (int i = 0; i < hotPacketSize; i++)
            {
                BigDecimal randomBigDecimal = getRandomRedPacketAmount(mu.doubleValue(), sigma, restAmount, size.intValue() - 1, minNum);
                restAmount = restAmount.subtract(randomBigDecimal);
                size = size.subtract(BigDecimal.ONE);
                mu = restAmount.divide(size, 2, BigDecimal.ROUND_HALF_DOWN);
                redPacketPool.add(randomBigDecimal);
            }
            redPacketPool.add(restAmount);
        } while (restAmount.compareTo(avg.multiply(maxMut)) > 0);
        //打乱红包顺序，因为越早的红包均值最高
        //倒序遍历list，然后在当前位置随机一个比当前位置小的int数字，交换数字
        Collections.shuffle(redPacketPool);
        return redPacketPool;
    }


    /**
     * 根据剩余红包金额均值，标准差大小，计算出随机红包的大小
     *
     * @param mu
     * @param sigma
     * @param rest     剩下的钱
     * @param restSize 还剩多少红包
     * @param minNum
     * @return
         */
    public static BigDecimal getRandomRedPacketAmount(double mu, double sigma, BigDecimal rest, int restSize, double minNum)
    {
        BigDecimal minValue = new BigDecimal(minNum);
        Random random = new Random();

        BigDecimal radomNo;
        //剩余最小的钱
        BigDecimal minRest = minValue.multiply(new BigDecimal(restSize));
        //随机出的红包也得满足剩余红包最少0.01
        do {
            radomNo = getRandom(mu, mu * sigma, random);
        }
        while (rest.subtract(radomNo).subtract(minRest).compareTo(BigDecimal.ZERO) < 0);
        if (rest.subtract(radomNo).subtract(minRest).compareTo(BigDecimal.ZERO) == 0) {
            return minValue;
        }
        BigDecimal randomBigDecimal = radomNo;
        //对红包金额取2位小数
        randomBigDecimal = randomBigDecimal.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        //判断金额不能小于0.01元
        randomBigDecimal = randomBigDecimal.compareTo(minValue) > 0 ? randomBigDecimal : minValue;
        return randomBigDecimal;
    }

    /**
     * 产生mu sigma的正态分布的double值
     *
     * @param mu
     * @param sigma
     * @return
     */
    public static BigDecimal getRandom(double mu, double sigma, Random random)
    {
        double randomValue = random.nextGaussian() * sigma + mu;
        BigDecimal value = new BigDecimal(String.valueOf(randomValue)).abs();
        return value;
    }
}
